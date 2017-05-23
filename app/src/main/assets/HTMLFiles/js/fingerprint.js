$(function() {

    var $tableBuild = $('#tableBuild');

    $tableBuild.bootstrapTable({
        idField: 'name',
        search: true,
        url: '/?type=getbuild',
        columns: [{
                field: 'enable',
                title: 'Enable',
                checkbox: 'true'
            },
            {
               field: 'type',
               title: 'From'
            },{
                field: 'name',
                title: 'Name'
            }, {
                field: 'value',
                title: 'Value'
            }, {
                field: 'newValue',
                title: 'New Value',
                editable: {
                    type: 'text',
                    mode: 'inline'
                }
            }
        ]
    });

    $tableBuild.on('all.bs.table', function(e, row) {
        addBuild(JSON.stringify($tableBuild.bootstrapTable('getData')));
    });
});

function addBuild(build) {
    $.get("/", {
        type: "addbuild",
        build: build
    }).done(function(data) {});
};

function resetFingerprint() {

        $.get("/", {
            type: "resetfingerprint"
        }).done(function( data ) {
           $("#tbs-content").load("/content/fingerprint.html");
        });
}