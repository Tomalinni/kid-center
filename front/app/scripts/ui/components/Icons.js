'use strict';

const React = require('react');


const GlyphIcon = ({name, iconClassName}) => {
    let className = 'glyphicon glyphicon-' + name;
    if (iconClassName) {
        className += ' ' + iconClassName;
    }
    return <span className={className} aria-hidden='true'></span>
};

const glyphs = {
    ok(className){
        return this.custom('ok', className)
    },
    save(className){
        return this.custom('save', className)
    },
    arrowLeft(className){
        return this.custom('arrow-left', className)
    },
    chevronLeft(className){
        return this.custom('chevron-left', className)
    },
    chevronRight(className){
        return this.custom('chevron-right', className)
    },
    check(className){
        return this.custom('check', className)
    },
    cog(className){
        return this.custom('cog', className)
    },
    signal(className){
        return this.custom('signal', className)
    },
    plus(className){
        return this.custom('plus', className)
    },
    minus(className){
        return this.custom('minus', className)
    },
    remove(className){
        return this.custom('remove', className)
    },
    move(className){
        return this.custom('move', className)
    },
    star(className){
        return this.custom('star', className)
    },
    pencil(className){
        return this.custom('pencil', className)
    },
    search(className){
        return this.custom('search', className)
    },
    user(className){
        return this.custom('user', className)
    },
    calendar(className){
        return this.custom('calendar', className)
    },
    camera(className){
        return this.custom('camera', className)
    },
    list(className){
        return this.custom('list', className)
    },
    newWindow(className){
        return this.custom('new-window', className)
    },
    menuUp(className){
        return this.custom('menu-up', className)
    },
    triangleTop(className){
        return this.custom('triangle-top', className)
    },
    triangleBottom(className){
        return this.custom('triangle-bottom', className)
    },
    logOut(className){
        return this.custom('log-out', className)
    },
    optionVertical(className){
        return this.custom('option-vertical', className)
    },
    banCircle(className){
        return this.custom('ban-circle', className)
    },
    refresh(className){
        return this.custom('refresh', className)
    },
    send(className){
        return this.custom('send', className)
    },
    warningSign(className){
        return this.custom('warning-sign', className)
    },
    dashboard(className){
        return this.custom('dashboard', className)
    },
    th(className){
        return this.custom('th', className)
    },
    export(className){
        return this.custom('export', className)
    },
    forward(className){
        return this.custom('forward', className)
    },
    backward(className){
        return this.custom('backward', className)
    },
    user(className){
        return this.custom('user', className)
    },
    listAlt(className){
        return this.custom('list-alt', className)
    },
    transfer(className){
        return this.custom('transfer', className)
    },
    eyeOpen(className){
        return this.custom('eye-open', className)
    },
    lock(className){
        return this.custom('lock', className)
    },

    custom(name, iconClassName){
        return <GlyphIcon name={name} iconClassName={iconClassName}/>
    }
};

module.exports = {
    glyph: glyphs,
    caret (){
        return <span className='caret' aria-hidden='true'></span>
    },
    spinner(){
        return <img src='/images/spinner.gif' aria-hidden='true'></img>
    },
    spinnerInverse(){
        return <img src='/images/spinner-inverse.gif' aria-hidden='true'></img>
    }
};
