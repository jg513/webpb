// Code generated by Webpb compiler, do not edit.
// https://github.com/jg513/webpb

import { Webpb } from 'webpb';

export interface IPageablePb {
    pagination?: boolean;
    page?: number;
    size?: number;
    sort?: string;
}

export class PageablePb implements IPageablePb {
    pagination?: boolean;
    page?: number;
    size?: number;
    sort?: string;
    META: () => Webpb.WebpbMeta;

    private constructor(p?: IPageablePb) {
        Webpb.assign(p, this, []);
        this.META = () => (p && {
            class: 'PageablePb',
            method: '',
            path: ''
        });
    }

    static create(properties: IPageablePb): PageablePb {
        return new PageablePb(properties);
    }
}

export interface IPagingPb {
    page: number;
    size: number;
    totalCount: number;
    totalPage: number;
}

export class PagingPb implements IPagingPb {
    page!: number;
    size!: number;
    totalCount!: number;
    totalPage!: number;
    META: () => Webpb.WebpbMeta;

    private constructor(p?: IPagingPb) {
        Webpb.assign(p, this, []);
        this.META = () => (p && {
            class: 'PagingPb',
            method: '',
            path: ''
        });
    }

    static create(properties: IPagingPb): PagingPb {
        return new PagingPb(properties);
    }
}
