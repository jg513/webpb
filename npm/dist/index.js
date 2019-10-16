"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var Webpb;
(function (Webpb) {
    function assign(src, dest, omitted) {
        if (src) {
            for (var ks = Object.keys(src), i = 0; i < ks.length; ++i) {
                if (src[ks[i]] != null && !isOmitted(ks[i], omitted)) {
                    dest[ks[i]] = src[ks[i]];
                }
            }
        }
    }
    Webpb.assign = assign;
    function isOmitted(k, omitted) {
        if (!omitted) {
            return false;
        }
        for (var _i = 0, omitted_1 = omitted; _i < omitted_1.length; _i++) {
            var o = omitted_1[_i];
            if (o === k) {
                return true;
            }
        }
        return false;
    }
    function getter(data, path) {
        if (!data) {
            return null;
        }
        for (var _i = 0, _a = path.split('.'); _i < _a.length; _i++) {
            var k = _a[_i];
            data = data[k];
            if (!data) {
                return null;
            }
        }
        return data;
    }
    Webpb.getter = getter;
    function query(params) {
        var str = '';
        // tslint:disable-next-line:forin
        for (var key in params) {
            var v = params[key];
            if (v === null) {
                continue;
            }
            str += str.length === 0 ? '?' : '&';
            str += key + "=" + v;
        }
        return str;
    }
    Webpb.query = query;
})(Webpb = exports.Webpb || (exports.Webpb = {}));
