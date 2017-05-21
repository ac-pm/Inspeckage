$(document).ready(function() {
    var trigger = $('.hamburger'),
        overlay = $('.overlay'),
        isClosed = false;

    trigger.click(function() {
        hamburger_cross();
    });

    function hamburger_cross() {

        if (isClosed == true) {
            overlay.hide();
            trigger.removeClass('is-open');
            trigger.addClass('is-closed');
            isClosed = false;
        } else {
            overlay.show();
            trigger.removeClass('is-closed');
            trigger.addClass('is-open');
            isClosed = true;
        }
    }

    $('[data-toggle="offcanvas"]').click(function() {
        $('#wrapper').toggleClass('toggled');


    });

    $("#tbs-content").load("/content/tabs.html");
});

$("#nav_tbs").on("click", function() {
    $("#tbs-content").load("/content/tabs.html");
    $("[name='refresh']").bootstrapSwitch('state', true);
});

$("#nav_replaces").on("click", function() {
    $("[name='refresh']").bootstrapSwitch('state', false);
    $("#tbs-content").load("/content/replace.html");

    showGeneralInfo(true);
});

$("#nav_location").on("click", function() {
    $("[name='refresh']").bootstrapSwitch('state', false);
    $("#tbs-content").load("/content/location.html");

    showGeneralInfo(true);
});



$("#nav_config").on("click", function() {
    $("[name='refresh']").bootstrapSwitch('state', false);
    $("#tbs-content").load("/content/fingerprint.html");
    showGeneralInfo(true);
});

$("#nav_source").on("click", function() {
    $("[name='refresh']").bootstrapSwitch('state', false);
    $("#tbs-content").load("/content/hooks.html");
    showGeneralInfo(true);
});

$("#nav_sponsor").on("click", function() {
    $("[name='refresh']").bootstrapSwitch('state', false);
    $("#tbs-content").load("/content/sponsor.html");
    showGeneralInfo(true);
});

$("#nav_about").on("click", function() {
    $("[name='refresh']").bootstrapSwitch('state', false);
    $("#tbs-content").load("/content/about.html");
    showGeneralInfo(true);
});

$("#nav_share").on("click", function() {
    $("[name='refresh']").bootstrapSwitch('state', false);
    $("#tbs-content").load("/content/share.html");
    showGeneralInfo(true);
});





$("#nav_howto").on("click", function() {
    $("[name='refresh']").bootstrapSwitch('state', false);
    $("#tbs-content").load("/content/tips.html");
    showGeneralInfo(true);
});
$("#nav_apk").on("click", function() {
    $("[name='refresh']").bootstrapSwitch('state', false);
    $("#tbs-content").load("/content/tips.html#nav-apk");
    showGeneralInfo(true);
});
$("#nav_proxy").on("click", function() {

    $("[name='refresh']").bootstrapSwitch('state', false);
    $("#tbs-content").load("/content/tips.html#nav-proxy");
    showGeneralInfo(true);
});
$("#nav_debug").on("click", function() {
    $("[name='refresh']").bootstrapSwitch('state', false);
    $("#tbs-content").load("/content/tips.html#nav-debug");
    showGeneralInfo(true);
});
$("#nav_hooks").on("click", function() {
    $("[name='refresh']").bootstrapSwitch('state', false);
    $("#tbs-content").load("/content/tips.html#nav-hooks");
    showGeneralInfo(true);
});

var shown = true;

        function showGeneralInfo(v) {

        if(v!=null){
            shown = v;
        }

          var general = $('#general-info');
          var generalInfoIcon = $('#general-info-icon');
          if(shown) {
            general.fadeOut();
            generalInfoIcon.attr("class", "fa fa-chevron-circle-down");
            shown = false;
          } else {
            general.fadeIn();
            generalInfoIcon.attr("class", "fa fa-chevron-circle-up");
            shown = true;
          }
        }




  $.fn.customerPopup = function (e, intWidth, intHeight, blnResize) {


    e.preventDefault();


    intWidth = intWidth || '500';
    intHeight = intHeight || '400';
    strResize = (blnResize ? 'yes' : 'no');


    var strTitle = ((typeof this.attr('title') !== 'undefined') ? this.attr('title') : 'Social Share'),
        strParam = 'width=' + intWidth + ',height=' + intHeight + ',resizable=' + strResize,
        objWindow = window.open(this.attr('href'), strTitle, strParam).focus();
  };



  $(document).ready(function ($) {
    $('.customer.share').on("click", function(e) {
      $(this).customerPopup(e);
    });
  });
