var $j = jQuery.noConflict();

document.observe("dom:loaded", function() {

    function log(text) {
        $("log").innerHTML = $("log").innerHTML + (!Object.isUndefined(text) && text !== null ? text.escapeHTML() : "null");

    }

    if (!window.WebSocket) {
        alert("WebSocket not natively supported.");
    }

    var ws;

    $("uriForm").observe("submit", function(e) {

        e.stop();

        var port = document.getElementById("wsport");
        ws = new WebSocket("ws://" + $F("uri") + ":" + port.textContent);

        ws.onopen = function() {
            log("[WebSocket Open]\n");
        };

        ws.onmessage = function(e) {
            log(e.data + "\n");
        };

        ws.onclose = function() {
            log("[WebSocket Close]\n");

            $("uri", "connect").invoke("enable");
            $("disconnect").disable();

            ws = null;
        };

        $("uri", "connect").invoke("disable");
        $("disconnect").enable();

    });

    $("disconnect").observe("click", function(e) {
        e.stop();
        if (ws) {
            ws.close();
            ws = null;
        }
    });

    $("startWS").observe("click", function(e) {

        $("startWS").disable();
        $("stopWS").enable();

        $("inlineCheckbox1").disable();
        $("inlineCheckbox2").disable();
        $("inlineCheckbox3").disable();
        $("inlineCheckbox4").disable();
        $("inlineCheckbox5").disable();
        $("inlineCheckbox6").disable();

    });

    $("stopWS").observe("click", function(e) {

        $("stopWS").disable();
        $("startWS").enable();

        $("inlineCheckbox1").enable();
        $("inlineCheckbox2").enable();
        $("inlineCheckbox3").enable();
        $("inlineCheckbox4").enable();
        $("inlineCheckbox5").enable();
        $("inlineCheckbox6").enable();
    });

    $("scroll").observe("click", function(e) {


       if($("scroll").value == "Stop Scroll"){
            stopScroll();
            $("scroll").value = 'Auto Scroll';
       }else if($("scroll").value == "Auto Scroll"){
            startScroll();
            $("scroll").value = 'Stop Scroll';
       }

    });

    $("clear").observe("click", function(e) {
        $("log").innerHTML = "clear...";
    });


});

var refresh;

function stopScroll() {
    clearInterval(refresh);
}

function startScroll() {
    refresh = setInterval(function(){ autoScroll() }, 1000);
}

function autoScroll() {
    $("log").scrollTop = $("log").scrollHeight;
}

function startWSocket() {

    var options = [];
    $j('#formx input:checked').each(function() {
        options.push($j(this).attr('value'));
    });

    $j.get("/", {
        type: "startWS",
        selected: "" + options

    });
}

function stopWSocket() {
    $j.get("/", {
        type: "stopWS"
    });
}