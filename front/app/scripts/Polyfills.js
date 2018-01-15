'use strict';

require('es6-promise').polyfill();
require('isomorphic-fetch');

Promise.prototype.finally = function (onResolveOrReject) {
    return this.catch(function (reason) {
        return reason;
    }).then(onResolveOrReject);
};

Promise.prototype.always = function (onResolveOrReject) {
    const p = this.then(val => {
            onResolveOrReject(val);
            return val
        },
        function (reason) {
            onResolveOrReject(reason);
            throw reason; //returns rejected promise in that case
        });
    p.resolve = this.resolve;
    p.reject = this.reject;
    return p;
};


Promise.defer = function () {
    let deferred = {};
    let p = new Promise(function (resolve, reject) {
        deferred.resolve = resolve;
        deferred.reject = reject;
    });
    p.resolve = deferred.resolve;
    p.reject = deferred.reject;
    return p;
};
