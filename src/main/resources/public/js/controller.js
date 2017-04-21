

// 控制逻辑
var Controller = {

  loginHandler: function(appname, password) {
    let data = ConfigService.login(appname, password);
    if (data.application === "*") {
      AppUserInfo.username = appname;
    } else {
      AppUserInfo.username = "user";
      AppUserInfo.appname = appname;
    }

    AppUserInfo.password = password;
    Render.renderConsole(data);
  },

  getApp: function(form) {
    let appname = $('#getapp-appname').val() || AppUserInfo.appname;
    let label = $('#getapp-label').val();
    let profile = $('#getapp-profile').val();
    let type = $('#getapp-type').val();
    if (appname == null || appname == "") {
      alert("未提供appname!");
      return false;
    }
    if (label == null || label == "") {
      alert("未提供label信息！");
      return false;
    }
    if (profile == null || profile == "") {
      alert("未提供profile!")
      return false;
    }

    let data = ConfigService.getApp(appname, label, profile, type);
    Render.renderCommonResult(data);

    return false;  // 防止页面刷新
  },

  createApp: function(form) {
    let appname = $('#createapp-appname').val();
    let password = $('#createapp-password').val();

    if (appname == null || appname == "") {
      alert("未提供appname!");
      return false;
    }
    if (password == null || password == "") {
      alert("未提供password！");
      return false;
    }

    if (ConfigService.createApp(appname, password)) {
      Render.renderCommonResult("## 应用`" + appname + "`创建成功！");
    }
    return false;
  },

  deleteApp: function(form) {
    let appname = $('#deleteapp-appname').val();
    if (appname == null || appname == "") {
      alert("未提供需要删除的appname！")
      return false;
    }

    ConfigService.deleteApp(appname);
    Render.renderCommonResult("## 应用`" + appname + "`删除成功！");
    return false;
  },

  encrypt: function(form) {
    let originText = $('#encrypt-origintext').val();
    if (originText == null || originText == "") {
      alert("未提供要加密的字符串！")
    }

    let data = ConfigService.encrypt(originText);
    data = "{cipher}" + data;
    Render.renderCommonResult(data);

    return false;
  },

  decrypt: function(form) {
    let decryptedText = $('#decrypt-origintext').val();
    if (decryptedText == null || decryptedText == "") {
      alert("未提供密文！")
    }

    let data = ConfigService.decrypt(decryptedText);
    Render.renderCommonResult(data);

    return false;
  }
};

