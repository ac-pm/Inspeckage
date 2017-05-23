$(document).ready(function() {

    document.cookie.split(";").forEach(function(c) { document.cookie = c.replace(/^ +/, "").replace(/=.*/, "=;expires=" + new Date().toUTCString() + ";path=/"); });

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

        CollapsibleLists.apply();

});

$(document.body).on('keyup', '#clipboard', function(){
        var chararcters = $("#clipboard").val();
         $.get("/", {
              type: "clipboard",
              value: chararcters
         });
 });

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

function saveLocation() {

    var geolocation = document.getElementById("loc").value;

        $.get("/", {
            type: "location",
            geolocation: geolocation
        }).done(function( data ) {
            if(data == "OK"){
                document.getElementById("savedLoc").innerHTML = geolocation;
            }
        });
}

$("[name='savedLoc']").bootstrapSwitch();
    $('input[name="savedLoc"]').on('switchChange.bootstrapSwitch', function(event, state) {

        console.log(state);
        $.get("/", {
            type: "geolocationSwitch",
            geolocationSwitch: state
        });

    });