'use strict';

const LocalStorageService = {
    tokenProp: 'token',
    localeProp: 'locale',

    getToken() {
        return localStorage.getItem(this.tokenProp)
    },
    setToken(val){
        localStorage.setItem(this.tokenProp, val)
    },
    removeToken(){
        localStorage.removeItem(this.tokenProp)
    },

    getLocale() {
        return localStorage.getItem(this.localeProp)
    },
    setLocale(val) {
        localStorage.setItem(this.localeProp, val)
    },
    removeLocale() {
        localStorage.removeItem(this.localeProp)
    },
};

//noinspection JSUnresolvedVariable
module.exports = LocalStorageService;
