'use strict';

const React = require('react'),
    ImageLoader = require('react-imageloader'),
    FileUpload = require('react-fileupload'),

    Config = require('../Config'),
    Utils = require('../Utils'),
    Icons = require('./Icons');

const AttachmentsList = React.createClass({
    messageHideTimeout: undefined,

    getInitialState() {
        return {
            statusMessage: ''
        };
    },
    componentWillUnmount() {
        if (this.messageHideTimeout) {
            clearTimeout(this.messageHideTimeout)
        }
    },
    render() {
        const self = this,
            props = this.props,

            fileUploadOptions = {
                fileFieldName: 'attachment',
                baseUrl: props.uploadUrl,
                multiple: true,
                chooseAndUpload: true,
                wrapperDisplay: 'block',

                chooseFile(files) {
                    self.setState({statusMessage: Utils.message('common.file.upload.files.selected', Utils.joinDefined(Utils.fileNames(files), ', '))})
                },
                beforeUpload(files) {
                    if (typeof files == 'string') return true;
                    if (files.length) {
                        let totalFileSize = 0;

                        for (let i = 0; i < files.length; i++) {
                            const file = files[i];
                            totalFileSize += file.size;

                            if (file.size > Config.uploadPhotoMaxSizeBytes) {
                                self.setState({statusMessage: Utils.message('common.file.upload.file.size.exceeded', file.name, Config.uploadPhotoMaxSizeMB)});
                                return false;
                            }
                        }

                        if (totalFileSize > Config.uploadPhotoMaxSizeBytes) {
                            self.setState({statusMessage: Utils.message('common.file.upload.total.file.size.exceeded', Config.uploadPhotoMaxSizeMB)});
                            return false;
                        }
                    }

                    return true;
                },
                doUpload(files) {
                    self.setState({statusMessage: Utils.message('common.file.upload.uploading.files', Utils.joinDefined(Utils.fileNames(files), ', '))});
                    console.log(self.state.statusMessage);
                },
                uploading(progress) {
                    self.setState({statusMessage: Utils.message('common.file.upload.uploading.progress', Math.floor(progress.loaded / progress.total * 100))});
                    console.log(self.state.statusMessage);
                },
                uploadSuccess(response) {
                    props.onFileUploaded.call(self, response.names);
                    self.setState({statusMessage: Utils.message('common.file.upload.uploading.success')});
                    console.log(self.state.statusMessage);
                    self.messageHideTimeout = setTimeout(()=> {
                        self.setState({statusMessage: ''});
                        console.log(self.state.statusMessage);
                    }, 10000);
                },
                uploadError(err) {
                    self.setState({statusMessage: Utils.message('common.file.upload.uploading.error', err.message)});
                    console.log(err);
                },
                uploadFail(response) {
                    self.setState({statusMessage: Utils.message('common.file.upload.uploading.error', (response.error || '') + ' ' + (response.exception || ''))});
                    console.log(response);
                }
            };

        return (
            <div className="attachments-list-container">
                {this.renderAttachmentButtons(fileUploadOptions)}
            </div>
        )
    },

    renderAttachmentButtons(fileUploadOptions) {
        const self = this,
            effectiveOptions = Utils.extend(fileUploadOptions, {withCredentials: false});
        return <div className="attachments-list-buttons">
            {self.renderAttachments() }
            {self.renderFileUpload(effectiveOptions) }
        </div>
    },

    renderFileUpload(effectiveOptions) {
        return <FileUpload options={effectiveOptions} className={'attachments-list-upload-container'}>
            <button className="btn btn-sm btn-default"
                    type="button"
                    ref="chooseAndUpload">{Icons.glyph.plus()}
            </button>
            <span className="attachments-list-status-message">
                    {this.state.statusMessage}
                </span>
        </FileUpload>
    },

    renderAttachments() {
        const props = this.props;
        return <div className="attachments-list-buttons-files">
            {props.fileNames.map((name)=> {
                const btnClass = props.shownFileName === name ? 'attachments-list-button-shown' : '',
                    primaryFileMark = props.primaryFileName === name ? (Icons.glyph.star()) : '';
                return <div className="removable-attachment" key={name}><a className={btnClass}
                                          href={props.downloadUrlFn.call(this, name)}>
                    {name}&nbsp;
                    {primaryFileMark}
                </a>
                    <button className="btn btn-xs btn-default"
                            onClick={props.onRemoveFile.bind(this, name)}
                            type="button">{Icons.glyph.minus()}
                    </button>
                </div>
            })}
        </div>
    }
});

module.exports = AttachmentsList;
