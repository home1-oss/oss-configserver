// var Remarkable = require('remarkable');
var mdAnalyzer = new Remarkable('full');

var mdAnalyzer = new Remarkable('full', {
  html: true,
  breaks: true,
  typographer: true
});

// 编译handlebars模板
function buildTemplate(tplId) {
  return Handlebars.compile($('#' + tplId).html());
};

// markdown解析
function pretty(str) {
  let preProcessed = "```\n"  + str + "\n```";
  return mdAnalyzer.render(preProcessed);
}

// 处理显示逻辑
var Render = {
  // 渲染控制台区域
  renderConsole: function(data) {
    let treeNodeItems;
    if (data.application === "*") {
      treeNodeItems = ADMIN_FUNC_ITEMS;
    } else {
      treeNodeItems = USER_FUNC_ITEMS;
    }

    $('#consoles').treeview({
      data: treeNodeItems,
      onNodeSelected: function(event, data) {
        // 绑定事件处理方法
        onConsoleNodeSelect(event, data);
      }
    });
  },

  // 以下方法渲染请求之后的区域
  renderCommonResult: function(data) {
    let prettyData = pretty(data);
    $('#common-result').html(prettyData);
  },


  // 以下方法，渲染用户通过左侧控制台、或者其他页面链接过来的请求页面

  renderLogin: function(loginType) {
    let type = loginType || 'user';
    let tpl = buildTemplate('login-template');
    let html = tpl({
      admin: type == "admin"
    });

    $('#panel-login').html(html);
    // 设置 select 选中状态
    $('#login-type').val(type);
  },

  renderListApp: function(title, appList) {
    let tpl = buildTemplate('listapp-template');
    let html = tpl({
      title: title,
      data: appList
    });

    $('#main-area').html(html);
  },

  beforeGetApp: function(title, appname) {
    let tpl = buildTemplate('getapp-template')
    let html = tpl({
      appname: appname,
      title: title
    });

    $('#main-area').html(html);
  },

  beforeCreateApp: function(title) {
    let tpl = buildTemplate('createapp-template');
    let html = tpl({
      title: title
    });

    $('#main-area').html(html);

    let tutorial = mdAnalyzer.render(CREATE_APP_STEPS);
    $('#createapp-tutorial').html(tutorial);
  },

  beforeDeleteApp: function(title, appname) {
    let tpl = buildTemplate('deleteapp-template');
    let html = tpl({
      appname: appname,
      title: title
    });

    $('#main-area').html(html);
  },

  beforeEncrypt: function(title) {
    let tpl = buildTemplate('encrypt-template');
    let html = tpl({
      title: title
    });

    $('#main-area').html(html);
  },

  beforeDecrypt: function(title) {
    let tpl = buildTemplate('decrypt-template');
    let html = tpl({
      title: title
    });

    $('#main-area').html(html);
  }
};

// 控制台事件 dispatch
var onConsoleNodeSelect = function(event, data) {
  switch (data.href) {
    case "/list_app":
      let appList = ConfigService.listApp();
      Render.renderListApp(data.text, appList);
      break;

    case "/get_app":
      Render.beforeGetApp(data.text, AppUserInfo.appname);
      break;

    case "/create_app":
      Render.beforeCreateApp(data.text);
      break;

    case "/delete_app":
      Render.beforeDeleteApp(data.text);
      break;

    case "/encrypt":
      Render.beforeEncrypt(data.text);
      break;

    case "/decrypt":
      Render.beforeDecrypt(data.text);
      break;
  }
};

Render.renderLogin("user");
