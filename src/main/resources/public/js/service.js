
// error event handler
function errorHandler(xhr, status) {
  if (status != null) {
    alert(status);
  } else {
    let msg = xhr.responseText;
    if (msg){
      msg = JSON.parse(msg);
      alert(msg.message);
    }
  }

  return false;
};

// ConfigService工具，封装ajax与config后端交互
var ConfigService = {
  login: function(username, password) {
    let loginProcessingUrl = "/config/users/login";
    let ret;
    $.ajax({
      url: loginProcessingUrl,
      async: false,
      method: "POST",
      headers: {
        'Authorization': 'Basic ' + btoa(username + ":" + password)
      },
      dataType: 'json',
      success: function(data, status, xhr) {
        ret = data;
      },
      error: function(xhr, status, error) {
        errorHandler(xhr, "登录失败!");
      }
    });

    return ret;
  },

  listApp: function() {
    let url = "/config/users/";
    let ret;
    $.ajax({
      url: url,
      method: "GET",
      async: false,
      headers: {
        'Authorization': 'Basic ' + btoa(AppUserInfo.username + ":" + AppUserInfo.password)
      },
      success: function(data, status, xhr) {
        ret = data;
      },
      error: function(xhr, status, error) {
        errorHandler(xhr);
      }
    });

    return ret;
  },

  createApp: function(appname, password) {
    let url = "/config/users/" + appname + "/";
    let data = { password: password };
    let ret = false;
    $.ajax({
      url: url,
      async: false,
      method: "POST",
      headers: {
        'Authorization': "Basic " + btoa(AppUserInfo.username + ":" + AppUserInfo.password),
        'Content-Type': "application/x-www-form-urlencoded"
      },
      data: data,
      success: function(data, status, xhr) {
        ret = true;
      },
      error: function(xhr, status, error) {
        errorHandler(xhr);
      }
    });

    return ret;
  },

  getApp: function(appname, label, profile, type) {
    let url = "/config/" + label + "/"+ appname + "-" + profile + "." + type;
    let ret;
    $.ajax({
      url: url,
      async: false,
      method: "GET",
      headers: {
        'Authorization': 'Basic ' + btoa(AppUserInfo.username + ":" + AppUserInfo.password)
      },
      success: function(data, status, xhr) {
        if (type == 'json') {
          ret = JSON.stringify(data, "", 4);
        } else {
          ret = data;
        }
      },
      error: function(xhr, status, error) {
        errorHandler(xhr);
      }
    });

    return ret;
  },

  deleteApp: function(appname) {
    let url =  "/config/users/" + appname + "/";
    let ret = false;
    $.ajax({
      url: url,
      async: false,
      method: 'DELETE',
      headers: {
        'Authorization': 'Basic ' + btoa(AppUserInfo.username + ":" + AppUserInfo.password)
      },
      success: function(data, status, xhr) {
        ret = true;
      },
      error: function(xhr, status, error) {
        errorHandler(xhr, "删除失败！");
      }
    });
    return ret;
  },

  encrypt: function(originText) {
    let url = "/config/encrypt";
    let ret;
    $.ajax({
      url: url,
      method: "POST",
      async: false,
      headers: {
        'Content-Type': "application/json;charset=UTF-8",
      },
      data: originText,
      success: function(data, status, xhr) {
        ret = data;
      },
      error: function(xhr, status, error) {
        errorHandler(xhr, "加密失败!");
      }
    });
    return ret;
  },

  decrypt: function(decryptedText) {
    let url = "/config/decrypt";
    let ret;
    $.ajax({
      url: url,
      async: false,
      method: "POST",
      headers: {
        'Content-Type': "application/json;charset=UTF-8",
      },
      data: decryptedText,
      success: function(data, status, xhr) {
        ret = data;
      },
      error: function(xhr, status, error) {
        errorHandler(xhr, "解密失败!");
      }
    });

    return ret;
  }
};

