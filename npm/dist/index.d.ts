export declare namespace Webpb {
    interface WebpbMessage {
        META(): WebpbMeta;
    }
    interface WebpbMeta {
        class: string;
        method: string;
        path: string;
    }
    function assign(src: any, dest: any, omitted?: string[]): void;
    function getter(data: any, path: string): any;
    function query(params: {
        [key: string]: any;
    }): string;
}
