// Code generated by Webpb compiler, do not edit.
// https://github.com/jg513/webpb

import { Webpb } from 'webpb';

import { ResourceProto } from './ResourceProto';

export namespace StoreProto {
    export interface IStorePb {
        id: number;
        name: string;
        city: number;
    }

    export class StorePb implements IStorePb {
        id!: number;
        name: string = "store";
        city: number = 100;
        META: () => Webpb.WebpbMeta;

        private constructor(p?: IStorePb) {
            Webpb.assign(p, this, []);
            this.META = () => (p && {
                class: 'StorePb',
                method: '',
                path: ''
            });
        }

        static create(properties: IStorePb): StorePb {
            return new StorePb(properties);
        }
    }

    export interface IProject {
    }

    export class Project implements IProject {
        META: () => Webpb.WebpbMeta;

        private constructor() {
            this.META = () => ({
                class: 'Project',
                method: '',
                path: ''
            });
        }

        static create(): Project {
            return new Project();
        }
    }

    export enum StoreType {
        NORMAL = 0,
    }

    export interface IStoreRequest {
        id: number;
        email: string;
        valid?: boolean;
        data: { [k: string]: number };
        projects: { [k: string]: StoreProto.IProject };
        unpacked: number[];
        packed: number[];
        projectList: StoreProto.IProject[];
        project: StoreProto.IProject;
        max: number;
        longMap: { [k: string]: number };
        projectMap: { [k: string]: StoreProto.IProject };
        typeMap: { [k: string]: StoreProto.StoreType };
        binary: Uint8Array;
        type: StoreProto.StoreType;
        floatData: number;
        stringField: string;
        anyName: string;
        anyProject: StoreProto.IProject;
        anyStore: StoreProto.StoreType;
    }

    export class StoreRequest implements IStoreRequest, Webpb.WebpbMessage {
        id!: number;
        email!: string;
        valid?: boolean;
        data!: { [k: string]: number };
        projects!: { [k: string]: StoreProto.IProject };
        unpacked!: number[];
        packed!: number[];
        projectList!: StoreProto.IProject[];
        project!: StoreProto.IProject;
        max: number = 0xfffffFFFFFFFF;
        longMap!: { [k: string]: number };
        projectMap!: { [k: string]: StoreProto.IProject };
        typeMap!: { [k: string]: StoreProto.StoreType };
        binary!: Uint8Array;
        type!: StoreProto.StoreType;
        floatData!: number;
        stringField!: string;
        anyName!: string;
        anyProject!: StoreProto.IProject;
        anyStore!: StoreProto.StoreType;
        META: () => Webpb.WebpbMeta;

        private constructor(p?: IStoreRequest) {
            Webpb.assign(p, this, ["id"]);
            this.META = () => (p && {
                class: 'StoreRequest',
                method: 'GET',
                path: `/stores/${p.id}`
            });
        }

        static create(properties: IStoreRequest): StoreRequest {
            return new StoreRequest(properties);
        }
    }

    export interface IStoreResponse {
        store: StoreProto.IStorePb;
        nested: StoreProto.StoreResponse.IStoreNestedPb;
    }

    export class StoreResponse implements IStoreResponse {
        store!: StoreProto.IStorePb;
        nested!: StoreProto.StoreResponse.IStoreNestedPb;
        META: () => Webpb.WebpbMeta;

        private constructor(p?: IStoreResponse) {
            Webpb.assign(p, this, []);
            this.META = () => (p && {
                class: 'StoreResponse',
                method: '',
                path: ''
            });
        }

        static create(properties: IStoreResponse): StoreResponse {
            return new StoreResponse(properties);
        }
    }

    export namespace StoreResponse {
        export interface IStoreNestedPb {
            employee: string;
        }

        export class StoreNestedPb implements IStoreNestedPb {
            employee!: string;
            META: () => Webpb.WebpbMeta;

            private constructor(p?: IStoreNestedPb) {
                Webpb.assign(p, this, []);
                this.META = () => (p && {
                    class: 'StoreNestedPb',
                    method: '',
                    path: ''
                });
            }

            static create(properties: IStoreNestedPb): StoreNestedPb {
                return new StoreNestedPb(properties);
            }
        }
    }

    export interface IStoreCurrentRequest {
    }

    export class StoreCurrentRequest implements IStoreCurrentRequest, Webpb.WebpbMessage {
        META: () => Webpb.WebpbMeta;

        private constructor() {
            this.META = () => ({
                class: 'StoreCurrentRequest',
                method: 'GET',
                path: `/stores/current`
            });
        }

        static create(): StoreCurrentRequest {
            return new StoreCurrentRequest();
        }
    }

    export interface IEmptyPb {
    }

    export class EmptyPb implements IEmptyPb {
        META: () => Webpb.WebpbMeta;

        private constructor() {
            this.META = () => ({
                class: 'EmptyPb',
                method: '',
                path: ''
            });
        }

        static create(): EmptyPb {
            return new EmptyPb();
        }
    }

    export namespace EmptyPb {
        export interface IEnclosingPb {
            innerString: string;
        }

        export class EnclosingPb implements IEnclosingPb {
            innerString!: string;
            META: () => Webpb.WebpbMeta;

            private constructor(p?: IEnclosingPb) {
                Webpb.assign(p, this, []);
                this.META = () => (p && {
                    class: 'EnclosingPb',
                    method: '',
                    path: ''
                });
            }

            static create(properties: IEnclosingPb): EnclosingPb {
                return new EnclosingPb(properties);
            }
        }
    }

    export interface IStoresRequest {
        pageable: ResourceProto.IPageablePb;
        type: number;
        city: number;
    }

    export class StoresRequest implements IStoresRequest, Webpb.WebpbMessage {
        pageable!: ResourceProto.IPageablePb;
        type!: number;
        city!: number;
        META: () => Webpb.WebpbMeta;

        private constructor(p?: IStoresRequest) {
            Webpb.assign(p, this, ["pageable", "type"]);
            this.META = () => (p && {
                class: 'StoresRequest',
                method: 'POST',
                path: `/stores/${p.type}${Webpb.query({
                    page: Webpb.getter(p, 'pageable.page'),
                    size: Webpb.getter(p, 'pageable.size'),
                })}`
            });
        }

        static create(properties: IStoresRequest): StoresRequest {
            return new StoresRequest(properties);
        }
    }

    export interface IStoresResponse {
        stores: StoreProto.IStorePb;
        paging: ResourceProto.IPagingPb;
    }

    export class StoresResponse implements IStoresResponse {
        stores!: StoreProto.IStorePb;
        paging!: ResourceProto.IPagingPb;
        META: () => Webpb.WebpbMeta;

        private constructor(p?: IStoresResponse) {
            Webpb.assign(p, this, []);
            this.META = () => (p && {
                class: 'StoresResponse',
                method: '',
                path: ''
            });
        }

        static create(properties: IStoresResponse): StoresResponse {
            return new StoresResponse(properties);
        }
    }

    export interface IUserMpLoginRequest {
        appId: string;
        code: string;
    }

    export class UserMpLoginRequest implements IUserMpLoginRequest {
        appId!: string;
        code!: string;
        META: () => Webpb.WebpbMeta;

        private constructor(p?: IUserMpLoginRequest) {
            Webpb.assign(p, this, []);
            this.META = () => (p && {
                class: 'UserMpLoginRequest',
                method: '',
                path: ''
            });
        }

        static create(properties: IUserMpLoginRequest): UserMpLoginRequest {
            return new UserMpLoginRequest(properties);
        }
    }
}
