$(document).ready(function() {

    getUserHooks();

    $('#prefs1').load('?type=file&value=prefs&count=-1');
    $('#accordion1').load('?type=file&value=pfiles&count=-1');
    $('#crypto1').load('?type=file&value=crypto&count=-1');
    $('#hash1').load('?type=file&value=hash&count=-1');
    $('#sqlite1').load('?type=file&value=sqlite&count=-1');
    $('#ipc1').load('?type=file&value=ipc&count=-1');
    $('#fs1').load('?type=file&value=fs&count=-1');
    $('#webview1').load('?type=file&value=wv&count=-1');
    $('#misc1').load('?type=file&value=misc&count=-1');
    $('#http1').load('?type=file&value=http&count=-1');
    $('#checkapp1').load('?type=checkapp&count=-1');
    $('#serialization1').load('?type=file&value=serialization&count=-1');
    $('#userhooks1').load('?type=file&value=userhooks&count=-1');


    $("[name='refresh']").bootstrapSwitch();
    $('input[name="refresh"]').on('switchChange.bootstrapSwitch', function(event, state) {

        if (state == true) {
            myStartFunction();
        } else if (state == false) {
            myStopFunction();
        }

    });


    $('a[data-toggle="tab"]').on('shown.bs.tab', function(e) {
        autoRefresh();
    });
});

var refresh;

function autoRefresh() {

    checkApp();

    var target = $('.nav-tabs .active > a').attr('href');

    if (target == '#sharedPreferences') {
        $("#badge_sharedPreferences").text("");
        $('#prefs1').load('?type=file&value=prefs&count=-1');
        $('#accordion1').load('?type=file&value=pfiles&count=-1');
        setCookie("prefs", "-2", 1);

    } else if (target == '#Crypto') {
        $("#badgeCrypto").text("");
        $('#crypto1').load('?type=file&value=crypto&count=-1');
        setCookie("crypto", "-2", 1);

    } else if (target == '#Hash') {
        $("#badgeHash").text("");
        $('#hash1').load('?type=file&value=hash&count=-1');
        setCookie("hash", "-2", 1);

    } else if (target == '#SQLite') {
        $("#badgeSqlite").text("");
        $('#sqlite1').load('?type=file&value=sqlite&count=-1');
        setCookie("sqlite", "-2", 1);

    } else if (target == '#http') {
        $("#badgeHttp").text("");
        $('#http1').load('?type=file&value=http&count=-1');
        setCookie("http", "-2", 1);

    } else if (target == '#fileSystem') {
        $("#badgeFs").text("");
        $('#fs1').load('?type=file&value=fs&count=-1');
        setCookie("fs", "-2", 1);
    } else if (target == '#serialization') {
        $("#badgeSerialization").text("");
        $('#serialization1').load('?type=file&value=serialization&count=-1');
        setCookie("serialization", "-2", 1);
    } else if (target == '#misc') {
        $("#badgeMisc").text("");
        $('#misc1').load('?type=file&value=misc&count=-1');
        setCookie("misc", "-2", 1);
    } else if (target == '#webView') {
        $("#badgeWeb").text("");
        $('#webview1').load('?type=file&value=wv&count=-1');
        setCookie("webv", "-2", 1);
    } else if (target == '#ipc') {
        $("#badgeIpc").text("");
        $('#ipc1').load('?type=file&value=ipc&count=-1');
        setCookie("ipc", "-2", 1);
    } else if (target == '#userHooks') {
        $("#badgeHooks").text("");
        $('#userhooks1').load('?type=file&value=userhooks&count=-1');
        setCookie("userHooks", "-2", 1);
    }


    if (target != '#sharedPreferences') {
        getPrefs();
    }

    if (target != '#serialization') {
        getSerialization();
    }

    if (target != '#Crypto') {
        getCrypto();
    }

    if (target != '#Hash') {
        getHash();
    }

    if (target != '#SQLite') {
        getSQLite();
    }

    if (target != '#http') {
        getHttp();
    }

    if (target != '#fileSystem') {
        getFS();
    }

    if (target != '#misc') {
        getMisc();
    }

    if (target != '#webView') {
        getWV();
    }

    if (target != '#ipc') {
        getIPC();
    }

    if (target != '#userHooks') {
        getUHooks();
    }

}

function myStopFunction() {
    clearInterval(refresh);
}

function myStartFunction() {
    refresh = setInterval(function() {
        autoRefresh()
    }, 1000);
}

function getPrefs() {
    var prefs = getCookie("prefs");

    $.get("/", {
        type: "file",
        value: "prefs",
        count: prefs
    }).done(function(data) {
        if (data != "") {

            if (prefs != -2) {
                if (data > prefs) {
                    setCookie("prefs", data, 1);
                    $("#badge_sharedPreferences").text(data);
                }
            } else {
                setCookie("prefs", data, 1);
            }
        }
    });
}

function getSQLite() {
    var sqlite = getCookie("sqlite");

    $.get("/", {
        type: "file",
        value: "sqlite",
        count: sqlite
    }).done(function(data) {
        if (data != "") {

            if (sqlite != -2) {
                if (data > sqlite) {
                    setCookie("sqlite", data, 1);
                    $("#badgeSqlite").text(data);
                }

            } else {
                setCookie("sqlite", data, 1);
            }
        }
    });
}

function getHash() {
    var hash = getCookie("hash");

    $.get("/", {
        type: "file",
        value: "hash",
        count: hash
    }).done(function(data) {
        if (data != "") {

            if (hash != -2) {
                if (data > hash) {
                    setCookie("hash", data, 1);
                    $("#badgeHash").text(data);
                }
            } else {
                setCookie("hash", data, 1);
            }
        }
    });
}


function getCrypto() {
    var crypto = getCookie("crypto");

    $.get("/", {
        type: "file",
        value: "crypto",
        count: crypto
    }).done(function(data) {
        if (data != "") {

            if (crypto != -2) {
                if (data > crypto) {
                    setCookie("crypto", data, 1);
                    $("#badgeCrypto").text(data);
                }
            } else {
                setCookie("crypto", data, 1);
            }
        }
    });
}

function getHttp() {
    var http = getCookie("http");

    $.get("/", {
        type: "file",
        value: "http",
        count: http
    }).done(function(data) {
        if (data != "") {

            if (http != -2) {
                if (data > http) {
                    setCookie("http", data, 1);
                    $("#badgeHttp").text(data);
                }
            } else {
                setCookie("http", data, 1);
            }
        }
    });
}

function getFS() {
    var fs = getCookie("fs");

    $.get("/", {
        type: "file",
        value: "fs",
        count: fs
    }).done(function(data) {
        if (data != "") {

            if (fs != -2) {
                if (data > fs) {
                    setCookie("fs", data, 1);
                    $("#badgeFs").text(data);
                }
            } else {
                setCookie("fs", data, 1);
            }
        }
    });
}

function getSerialization() {
    var serialization = getCookie("serialization");

    $.get("/", {
        type: "file",
        value: "serialization",
        count: serialization
    }).done(function(data) {
        if (data != "") {

            if (serialization != -2) {
                if (data > serialization) {
                    setCookie("serialization", data, 1);
                    $("#badgeSerialization").text(data);
                }
            } else {
                setCookie("serialization", data, 1);
            }
        }
    });
}

function getWV() {
    var webv = getCookie("webv");

    $.get("/", {
        type: "file",
        value: "wv",
        count: webv
    }).done(function(data) {
        if (data != "") {

            if (webv != -2) {
                if (data > webv) {
                    setCookie("webv", data, 1);
                    $("#badgeWeb").text(data);
                }
            } else {
                setCookie("webv", data, 1);
            }
        }
    });
}

function getIPC() {
    var ipc = getCookie("ipc");

    $.get("/", {
        type: "file",
        value: "ipc",
        count: ipc
    }).done(function(data) {
        if (data != "") {

            if (ipc != -2) {
                if (data > ipc) {
                    setCookie("ipc", data, 1);
                    $("#badgeIpc").text(data);
                }
            } else {
                setCookie("ipc", data, 1);
            }
        }
    });
}

function getUHooks() {
    var userHooks = getCookie("userHooks");

    $.get("/", {
        type: "file",
        value: "userhooks",
        count: userHooks
    }).done(function(data) {
        if (data != "") {

            if (userHooks != -2) {
                if (data > userHooks) {
                    setCookie("userHooks", data, 1);
                    $("#badgeHooks").text(data);
                }
            } else {
                setCookie("userHooks", data, 1);
            }
        }
    });
}

function getMisc() {
    var misc = getCookie("misc");

    $.get("/", {
        type: "file",
        value: "misc",
        count: misc
    }).done(function(data) {
        if (data != "") {

            if (misc != null && misc > 0) {
                if (data > misc) {
                    setCookie("misc", data, 1);
                    $("#badgeMisc").text(data);
                }
            } else {
                setCookie("misc", data, 1);
            }
        }
    });
}

function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i].trim();
        if (c.indexOf(name) == 0) return c.substring(name.length, c.length);
    }
    return null;
}

function setCookie(c_name, value, exdays) {
    var exdate = new Date();
    exdate.setDate(exdate.getDate() + exdays);
    var c_value = escape(value) + ((exdays == null) ? "" : "; expires=" + exdate.toUTCString());
    document.cookie = c_name + "=" + c_value;
}


function checkApp() {
    $('#checkapp1').load('?type=checkapp');
}

$("[name='shared']").bootstrapSwitch();
    $('input[name="shared"]').on('switchChange.bootstrapSwitch', function(event, state) {
        $.get("/", {
           type: "enableTab",
           tab: "shared",
           value: state
        });
    });

$("[name='serialization']").bootstrapSwitch();
    $('input[name="serialization"]').on('switchChange.bootstrapSwitch', function(event, state) {
        $.get("/", {
           type: "enableTab",
           tab: "serialization",
           value: state
        });
    });

    $("[name='crypto']").bootstrapSwitch();
        $('input[name="crypto"]').on('switchChange.bootstrapSwitch', function(event, state) {
            $.get("/", {
               type: "enableTab",
               tab: "crypto",
               value: state
            });
        });

    $("[name='hash']").bootstrapSwitch();
        $('input[name="hash"]').on('switchChange.bootstrapSwitch', function(event, state) {
            $.get("/", {
               type: "enableTab",
               tab: "hash",
               value: state
            });
        });

    $("[name='sqlite']").bootstrapSwitch();
        $('input[name="sqlite"]').on('switchChange.bootstrapSwitch', function(event, state) {
            $.get("/", {
               type: "enableTab",
               tab: "sqlite",
               value: state
            });
        });

    $("[name='http']").bootstrapSwitch();
        $('input[name="http"]').on('switchChange.bootstrapSwitch', function(event, state) {
            $.get("/", {
               type: "enableTab",
               tab: "http",
               value: state
            });
        });

    $("[name='filesystem']").bootstrapSwitch();
        $('input[name="filesystem"]').on('switchChange.bootstrapSwitch', function(event, state) {
            $.get("/", {
               type: "enableTab",
               tab: "filesystem",
               value: state
            });
        });

    $("[name='misc']").bootstrapSwitch();
        $('input[name="misc"]').on('switchChange.bootstrapSwitch', function(event, state) {
            $.get("/", {
               type: "enableTab",
               tab: "misc",
               value: state
            });
        });

    $("[name='webview']").bootstrapSwitch();
        $('input[name="webview"]').on('switchChange.bootstrapSwitch', function(event, state) {
            $.get("/", {
               type: "enableTab",
               tab: "webview",
               value: state
            });
        });

    $("[name='ipc']").bootstrapSwitch();
        $('input[name="ipc"]').on('switchChange.bootstrapSwitch', function(event, state) {
            $.get("/", {
               type: "enableTab",
               tab: "ipc",
               value: state
            });
        });

    $("[name='phooks']").bootstrapSwitch();
        $('input[name="phooks"]').on('switchChange.bootstrapSwitch', function(event, state) {
            $.get("/", {
               type: "enableTab",
               tab: "phooks",
               value: state
            });
        });
