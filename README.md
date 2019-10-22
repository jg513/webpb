# webpb
Generate api definitions for web framework from protocol buffers file

## Cli tool 

### Options

- usage

```shell script

java -jar webpb.jar
Missing required options [--type=<type>, --out=<out>, --proto_path=<protoPaths>]
Usage: webpb [-hV] --out=<out> [--quiet=<quiet>] --type=<type>
             [--excludes=<excludes>...]... [--files=<files>...]...
             [--includes=<includes>...]... --proto_path=<protoPaths>...
             [--proto_path=<protoPaths>...]...
      --excludes=<excludes>...
                           Excluded paths.
      --files=<files>...   Source files.
  -h, --help               Show this help message and exit.
      --includes=<includes>...
                           Included paths.
      --out=<out>          Generated code output directory.
      --proto_path=<protoPaths>...
                           Paths to resolving proto files.
      --quiet=<quiet>      Hide logs.
      --type=<type>        TS, JAVA.
  -V, --version            Print version information and exit.
```

- example

`java -jar webpb.jar --type=JAVA --proto_path=example/proto --out=/tmp/protocol`

## Integration

### Definition

```proto
message StoresRequest {
    option (method) = "POST";

    option (path) = "/stores/{type}?page={paging.page}&size={paging.size}";

    required ResourceProto.PageablePb pageable = 1 [(omitted) = true];
    required int32 type = 2 [(omitted) = true];
    required int32 city = 3 [(java_anno) = '@NotNull(message = "City is required")', (java_anno) = '@Range(min = 0)'];
}
```

### Web

#### NPM package 
https://www.npmjs.com/package/webpb

#### Sample request

- Generated typescript code

```typescript
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

    private constructor(p: IStoresRequest) {
        Webpb.assign(p, this, ["pageable", "type"]);
        this.META = () => ({
            class: 'StoresRequest',
            method: 'POST',
            path: `/stores/${p.type}${Webpb.query({
                page: Webpb.getter(p, 'paging.page'),
                size: Webpb.getter(p, 'paging.size'),
            })}`
        });
    }

    static create(properties: IStoresRequest): StoresRequest {
        return new StoresRequest(properties);
    }
}
```

- Request with some http client, given `feignClient`

```typescript
const message = StoresRequest.create({
    pageable: {
        page: 1,
        size: 10
    },
    city: 100,
    type: 1
});

let meta = message.META();
feignClient.request({
    method: meta.method,
    url: meta.path,
    body: message
})
```

### Spring boot

#### Maven package

#### Sample request mapping

- Generated java code

```java
@Setter
@Getter
@Accessors(chain = true)
public class StoresRequest implements WebpbMessage {

    public static final String METHOD = "POST";

    public static final String PATH = "/stores/{type}";

    @NotNull(message = "City is required")
    @Range(min = 0)
    private Integer city;
}
```

- Register bean

```java
@Bean
@Order(0)
public WebMvcRegistrations webMvcRegistrationsHandlerMapping() {
    return new WebMvcRegistrations() {
        @Override
        public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
            return new WebpbRequestControllerMapping();
        }
    };
}
```

- Controller

```java
@WebpbMapping
public void findAll(Pageable pageable, @Valid @RequestBody StoresRequest request) {
    return storeService.findAll(pageable, request);
}
```
