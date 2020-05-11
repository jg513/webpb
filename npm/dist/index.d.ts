export interface WebpbMessage {
    META(): WebpbMeta;
}
export interface WebpbMeta {
    class: string;
    method: string;
    path: string;
}
export declare function assign(src: any, dest: any, omitted?: string[]): void;
export declare function getter(data: any, path: string): any;
export declare function query(params: {
    [key: string]: any;
}): string;
