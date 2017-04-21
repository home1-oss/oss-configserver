'use strict';

var AppUserInfo = {};

const ADMIN_FUNC_ITEMS =  [
  {
    text: "应用管理",
    selectable: false,
    nodes: [
      {text: "应用列表", href: "/list_app"},
      {text: "应用详情", href: "/get_app"},
      {text: "新增应用", href: "/create_app"},
      {text: "删除应用", href: "/delete_app"}
    ]
  },
  {
    text: "加解密工具", 
    selectable: false,
    nodes: [
      {text: "加密工具", href: "/encrypt"},
      {text: "解密工具", href: "/decrypt"}
    ]
  }
];

const USER_FUNC_ITEMS = [
  {text: "应用详情", href: "/get_app"},
  {text: "加密工具", href: "/encrypt"}
];

const CREATE_APP_STEPS = `
### 确保应用名称未被使用
- 应用名称为应用的 **\${spring.application.name}**
- 应用应具备相应的域名
- 开发/测试环境为 **\${spring.application.name}.internal**
- 生成环境为 **\${spring.application.name}.idc**

### 为应用生成一个随机密码
- 方法为 TODO

### 为应用创建用户
- 方法为 TODO

### 在git服务上为应用创建配置repository
- 名称为: **\${spring.application.name}-config**
- 内容为: TODO

### 为配置repository设置webhook
- 需设置开发/测试环境及生产环境的webhook

### 为配置repository设置deploy key
- 需设置开发/测试环境及生产环境的deploy key
`;
