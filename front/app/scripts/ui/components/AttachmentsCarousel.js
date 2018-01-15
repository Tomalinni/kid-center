'use strict';

const React = require('react'),
    ImageLoader = require('react-imageloader'),
    Config = require('../Config'),
    Utils = require('../Utils'),
    Actions = require('../actions/Actions'),
    Icons = require('./Icons');

const {PropTypes} = React;

const AttachmentsCarousel = React.createClass({

    propTypes: {
        onHideFn: PropTypes.func
    },

    render() {
        const self = this;
        return <div>
            {self.renderCarousel()}
        </div>
    },

    renderCarousel(){
        const self = this, p = self.props;
        if (p.showCarousel && Utils.isNotEmptyArray(p.shownAttachments)) {
            return <div id="attachmentsCarousel" className="carousel photos-carousel-fullscreen"
                        data-ride="carousel">
                <button type="button"
                        className="btn photos-carousel-close-btn"
                        onClick={() => {
                            if (p.onHideFn) {
                                p.onHideFn();
                            }
                            self.setState({showCarouselInternal: false});
                        }}>
                    {Icons.glyph.remove()}
                </button>

                <ol className="carousel-indicators">
                    {p.shownAttachments.map((attachment, i) => {
                        return <li key={i}
                                   data-target="#attachmentsCarousel" data-slide-to={0}
                                   className={i === 0 ? "active" : ""}/>
                    })}
                </ol>

                <div className="carousel-inner" role="listbox">
                    {p.shownAttachments.map((attachment, i) => {
                        const classNames = ['item'];
                        if (i === 0) {
                            classNames.push('active')
                        }
                        return <div key={i}
                                    className={Utils.joinDefined(classNames)}>
                            <div className="carousel-item-container">
                                {self.isImage(attachment) ?
                                    <ImageLoader
                                        src={p.currentAttachmentUrl(attachment)}
                                        imgProps={{
                                            className: 'carousel-photo',
                                            alt: 'Photo ' + i
                                        }}
                                        preloader={Icons.spinnerInverse}>
                                        {Utils.message('common.image.load.failed')}
                                    </ImageLoader> :
                                    <div className="download-file-gallery">
                                        <a target="_blank" href={p.currentAttachmentUrl(attachment)}>
                                            <div className="glyphicon glyphicon-download"/>
                                            <br/>
                                            <span>{attachment}</span>
                                        </a>
                                    </div>}
                            </div>
                        </div>
                    })}
                </div>

                <a className="left carousel-control" href="#attachmentsCarousel" role="button" data-slide="prev">
                    <span className="glyphicon glyphicon-chevron-left" aria-hidden="true"/>
                </a>
                <a className="right carousel-control" href="#attachmentsCarousel" role="button" data-slide="next">
                    <span className="glyphicon glyphicon-chevron-right" aria-hidden="true"/>
                </a>
            </div>
        }
    },

    isImage(fileName) {
        const parts = fileName.split('.'),
            extension = '.' + parts[parts.length - 1];
        return [".jpg", ".jpeg", ".bmp", ".gif", ".png"].includes(extension.toLowerCase());
    }
});

module.exports = AttachmentsCarousel;