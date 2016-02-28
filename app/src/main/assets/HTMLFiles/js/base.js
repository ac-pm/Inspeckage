$(document).ready(function() {

    $('#prefs1').load('?type=file&value=prefs');
    $('#accordion1').load('?type=file&value=pfiles');
    $('#crypto1').load('?type=file&value=crypto');
    $('#hash1').load('?type=file&value=hash');
    $('#sqlite1').load('?type=file&value=sqlite');
    $('#ipc1').load('?type=file&value=ipc');
    $('#fs1').load('?type=file&value=fs');
    $('#webview1').load('?type=file&value=wv');
    $('#misc1').load('?type=file&value=misc');
    $('#http1').load('?type=file&value=http');
    $('#checkapp1').load('?type=checkapp');
    $('#serialization1').load('?type=file&value=serialization');


    $("[name='ssl_uncheck']").bootstrapSwitch();
    $('input[name="ssl_uncheck"]').on('switchChange.bootstrapSwitch', function(event, state) {

        console.log(state);
        $.get("/", {
            type: "sslunpinning",
            sslswitch: state
        });

    });


    $("[name='flag_sec']").bootstrapSwitch();
    $('input[name="flag_sec"]').on('switchChange.bootstrapSwitch', function(event, state) {
        console.log(state);
        $.get("/", {
            type: "flagsec",
            fsswitch: state
        });

    });

    $("[name='switch_proxy']").bootstrapSwitch();
    $('input[name="switch_proxy"]').on('switchChange.bootstrapSwitch', function(event, state) {
        console.log(state);
        $.get("/", {
            type: "switchproxy",
            value: state
        });

    });


    $("[name='exported']").bootstrapSwitch();
        $('input[name="exported"]').on('switchChange.bootstrapSwitch', function(event, state) {
            console.log(state);
            $.get("/", {
                type: "exported",
                value: state
            });

        });

        $("[name='refresh']").bootstrapSwitch();
                $('input[name="refresh"]').on('switchChange.bootstrapSwitch', function(event, state) {
                    console.log(state);

                    if(state == true){
                        myStartFunction();
                    }else if(state == false){
                        myStopFunction();
                    }

                });

        setInterval(function(){ checkApp() }, 6000);

        CollapsibleLists.apply();

});

var refresh;

function autoRefresh() {
                $('#prefs1').load('?type=file&value=prefs');
                $('#accordion1').load('?type=file&value=pfiles');
                $('#crypto1').load('?type=file&value=crypto');
                $('#hash1').load('?type=file&value=hash');
                $('#sqlite1').load('?type=file&value=sqlite');
                $('#ipc1').load('?type=file&value=ipc');
                $('#fs1').load('?type=file&value=fs');
                $('#webview1').load('?type=file&value=wv');
                $('#misc1').load('?type=file&value=misc');
                $('#http1').load('?type=file&value=http');
                $('#serialization1').load('?type=file&value=serialization');
}

function myStopFunction() {
    clearInterval(refresh);
}

function myStartFunction() {
    refresh = setInterval(function(){ autoRefresh() }, 6000);
}

function checkApp() {
    $('#checkapp1').load('?type=checkapp');
}

function fileTree() {
    $('#fileTree1').load('?type=filetree');
}

function finishApp(){
    $.get("/", {
                type: "finishapp"
            });
}

function restartApp() {
    $.get("/", {
                type: "restartapp"
            });
}

function startApp() {
    $.get("/", {
                type: "startapp"
            });
}

function setARP() {

    var ip = document.getElementById("ip").value;
    var mac = document.getElementById("mac").value;
    var ipformat = /^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;

        if (document.getElementById("ip").value.match(ipformat)) {

            $.get("/", {
                type: "setarp",
                ip: ip,
                mac: mac
            });

            return true;
        } else {
            alert("You have entered an invalid IP address!");
        }
}

function proxyTest() {
    $('#proxyTest').load('?type=proxytest');
}

function screenshot() {
    window.location = "/?type=screenshot";
}

function download_apk() {
    window.location = "/?type=downapk";
}

function download_all() {
    window.location = "/?type=downall";
}

function download_file(path) {

    window.location = "/?type=downloadfile&value="+path;
}

function setProxy() {

    var host = document.getElementById("host").value;
    var port = document.getElementById("port").value;
    var ipformat = /^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;

    if (document.getElementById("host").value.match(ipformat)) {

        $.get("/", {
            type: "proxy",
            host: host,
            port: port
        });

        return true;
    } else {
        alert("You have entered an invalid IP address!");
    }
}

function startComplexActivity() {

    var txt_activity = document.getElementById("txt_activity").value;
    var txt_action = document.getElementById("txt_action").value;
    var txt_category = document.getElementById("txt_category").value;
    var txt_datauri = document.getElementById("txt_datauri").value;
    var txt_extra = document.getElementById("txt_extra").value;
    var txt_flags = document.getElementById("txt_flags").value;
    var txt_mimetype = document.getElementById("txt_mimetype").value;

    $.get("/", {
       type: "start",
       component: "activity",
       activity: txt_activity,
       action: txt_action,
       category: txt_category,
       datauri: txt_datauri,
       extra: txt_extra,
       flags: txt_flags,
       mimetype: txt_mimetype
    });
}

function queryProvider(){

    var txt_uri = document.getElementById("txt_uri").value;

    $.get("/", {
           type: "start",
           component: "provider",
           uri: txt_uri
        });
}

function selectAct(act){
    document.getElementById("txt_activity").value = act;
}

function selectAction(act){
    document.getElementById("txt_action").value = act;
}

function selectCategory(act){
    document.getElementById("txt_category").value = act;
}

function selectFlag(act){
    document.getElementById("txt_flags").value = act;
}

function startActivity(act){
        $.get("/", {
             type: "start",
             component: "activity",
             activity: act
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

