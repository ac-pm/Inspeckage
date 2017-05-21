$(document).ready(function() {

    var line = "----------------------------------------HookTable------------------------------------------------------";

    var $hooktable = $('#hooktable'),
        $button = $('#buttonInsertHook');

    $(function() {

        $button.click(function() {
            var randomId = 1000 + ~~(Math.random() * 1000);

            var className = document.getElementById("className").value;
            var method = document.getElementById("method").value;

            var data = $hooktable.bootstrapTable('getData');
             for (index in data) {
                            if (data[index].className == className && data[index].method == method) {

                                return;
                            }
                        }

            $hooktable.bootstrapTable('insertRow', {
                index: 0,
                row: {
                    state: true,
                    id: randomId,
                    className: document.getElementById("className").value,
                    method: document.getElementById("method").value,
                    constructor: $("#hookConstructor").is(":checked")
                }
            });
            console.log(JSON.stringify($hooktable.bootstrapTable('getData')));
            addUserHooks(JSON.stringify($hooktable.bootstrapTable('getData')));
        });
    });

    $('#hooktable').on('uncheck.bs.table', function(e, row) {
        console.log(JSON.stringify($hooktable.bootstrapTable('getData')));
        addUserHooks(JSON.stringify($hooktable.bootstrapTable('getData')));
    });

    $('#hooktable').on('check.bs.table', function(e, row) {
        console.log(JSON.stringify($hooktable.bootstrapTable('getData')));
        addUserHooks(JSON.stringify($hooktable.bootstrapTable('getData')));
    });

    var line = "----------------------------------------ReplaceTable------------------------------------------------------";

    getUserHooks();

    var $table = $('#table'),
        $button2 = $('#buttonInsert');

    $(function() {
        $button2.click(function() {

            var position = document.getElementById("position").value;
            var classMethod = document.getElementById("classMethod").value;
            var paramMatch = document.getElementById("paramMatch").value;

            var data = $table.bootstrapTable('getData');
            for (index in data) {
                if (data[index].position == position && data[index].classMethod == classMethod && data[index].paramMatch == paramMatch) {
                    $('#alertParamText').html('<strong>Warning!</strong> There is this position!');
                    $('#alertParam').slideDown();
                    return;
                }
            }

            var paramType = document.getElementById("paramType").value;
            var paramNewValue = document.getElementById("paramNewValue").value;
            if (paramType != 'String' && paramType != 'ByteArray') {
                if (paramNewValue == '') {
                    $('#alertParamText').html('<strong>Warning!</strong> The New Value is required!');
                    $('#alertParam').slideDown();
                    return;
                }
            }



            var randomId = 1000 + ~~(Math.random() * 1000);
            $table.bootstrapTable('insertRow', {
                index: 0,
                row: {
                    id: randomId,
                    state: true,
                    classMethod: document.getElementById("classMethod").value,
                    position: document.getElementById("position").value,
                    paramType: document.getElementById("paramType").value,
                    paramMatch: document.getElementById("paramMatch").value,
                    paramNewValue: document.getElementById("paramNewValue").value
                }
            });
            console.log(JSON.stringify($table.bootstrapTable('getData')));
            addUserReplaces(JSON.stringify($table.bootstrapTable('getData')));

        });
    });

    $('#alertParam .close').on('click', function() {
        $('#alertParam').slideUp();
    });

    $('#table').on('uncheck.bs.table', function(e, row) {
        console.log(JSON.stringify($table.bootstrapTable('getData')));
        addUserReplaces(JSON.stringify($table.bootstrapTable('getData')));
    });

    $('#table').on('check.bs.table', function(e, row) {
        console.log(JSON.stringify($table.bootstrapTable('getData')));
        addUserReplaces(JSON.stringify($table.bootstrapTable('getData')));
    });

    var line = "----------------------------------------Replace Return Table------------------------------------------------------";

    var $tableReturn = $('#tableReturn'),
        $button3 = $('#buttonInsertReturn');

    $(function() {
        $button3.click(function() {


            var returnType = document.getElementById("returnType").value;
            if (returnType == 'void') {
                $('#alertReturnText').html('<strong>Warning!</strong> Please choose one type!');
                $('#alertReturn').slideDown();
                return;
            }

            var classMethodReturn = document.getElementById("classMethodReturn").value;
            var dataReturn = $tableReturn.bootstrapTable('getData');
            var returnMatch = document.getElementById("returnMatch").value;
            for (index in dataReturn) {
                if (dataReturn[index].classMethod == classMethodReturn && (returnMatch == null || returnMatch == '')) {
                    $('#alertReturnText').html('<strong>Warning!</strong> There is this return!');
                    $('#alertReturn').slideDown();
                    return;
                }
            }



            var returnNewValue = document.getElementById("returnNewValue").value;
            if (returnType != 'void' && returnType != 'String') {
                if (returnNewValue == '') {
                    $('#alertReturnText').html('<strong>Warning!</strong> The New Value is required!');
                    $('#alertReturn').slideDown();
                    return;
                }
            }

            var randomId = 1000 + ~~(Math.random() * 1000);
            $tableReturn.bootstrapTable('insertRow', {
                index: 0,
                row: {
                    id: randomId,
                    state: true,
                    classMethod: document.getElementById("classMethodReturn").value,
                    returnType: document.getElementById("returnType").value,
                    returnMatch: document.getElementById("returnMatch").value,
                    returnNewValue: document.getElementById("returnNewValue").value
                }
            });
            console.log('Add Return: ' + JSON.stringify($tableReturn.bootstrapTable('getData')));
            addUserReplacesReturn(JSON.stringify($tableReturn.bootstrapTable('getData')));
        });
    });

    $('#alertReturn .close').on('click', function() {
        $('#alertReturn').slideUp();
    });

    $('#tableReturn').on('uncheck.bs.table', function(e, row) {
        console.log('Update Return: ' + JSON.stringify($tableReturn.bootstrapTable('getData')));
        addUserReplacesReturn(JSON.stringify($tableReturn.bootstrapTable('getData')));
    });

    $('#tableReturn').on('check.bs.table', function(e, row) {
        console.log('Update Return: ' + JSON.stringify($tableReturn.bootstrapTable('getData')));
        addUserReplacesReturn(JSON.stringify($tableReturn.bootstrapTable('getData')));
    });

});


var line = "----------------------------------------HookTable Remove------------------------------------------------------";

function actionFormatter(value, row, index) {
    return [
        '<a class="remove ml10" href="javascript:void(0)" title="Remove">',
        '<i class="glyphicon glyphicon-remove"></i>',
        '</a>'
    ].join('');
};

window.actionEvents = {
    'click .remove': function(e, value, row, index) {
        console.log('Remove hook id: ' + row.id);
        $('#hooktable').bootstrapTable('removeByUniqueId', row.id);
        addUserHooks(JSON.stringify($('#hooktable').bootstrapTable('getData')));
    }
};

var line = "----------------------------------------Replace Table Remove------------------------------------------------------";

function actionFormatterReplace(value, row, index) {
    return [
        '<a class="removeR ml10" href="javascript:void(0)" title="Remove">',
        '<i class="glyphicon glyphicon-remove"></i>',
        '</a>'
    ].join('');
};

window.actionEventsReplace = {
    'click .removeR': function(e, value, row, index) {

        $('#table').bootstrapTable('removeByUniqueId', row.id);
        console.log(JSON.stringify($('#table').bootstrapTable('getData')));
        addUserReplaces(JSON.stringify($('#table').bootstrapTable('getData')));
    }
};

var line = "----------------------------------------Return Table Remove------------------------------------------------------";

function actionFormatterReturn(value, row, index) {
    return [
        '<a class="removeReturn ml10" href="javascript:void(0)" title="Remove">',
        '<i class="glyphicon glyphicon-remove"></i>',
        '</a>'
    ].join('');
};

window.actionEventsReturn = {
    'click .removeReturn': function(e, value, row, index) {

        $('#tableReturn').bootstrapTable('removeByUniqueId', row.id);
        console.log('Remove Return: ' + JSON.stringify($('#tableReturn').bootstrapTable('getData')));
        addUserReplacesReturn(JSON.stringify($('#tableReturn').bootstrapTable('getData')));
    }
};

var line = "----------------------------------------Requests------------------------------------------------------";


function addUserHooks(jhooks) {
    $.get("/", {
        type: "adduserhooks",
        jhooks: jhooks
    }).done(function(data) {

    });
};

function getUserHooks() {
    $.get("/", {
        type: "getuserhooks"
    }).done(function(data) {



        for (index in data) {
            if (data[index].state) {
                $('#classMethod').append('<option value="' + data[index].className + '.' + data[index].method + '">' + data[index].className + '.' + data[index].method + '</option>');
                $('#classMethodReturn').append('<option value="' + data[index].className + '.' + data[index].method + '">' + data[index].className + '.' + data[index].method + '</option>');
            }
        }
    });
};

function addUserReplaces(data) {
    $.get("/", {
        type: "addparamreplaces",
        data: data
    }).done(function(data) {

    });
};

function addUserReplacesReturn(data) {
    $.get("/", {
        type: "addreturnreplaces",
        data: data
    }).done(function(data) {

    });
};

function loadReplaces() {
    $("#replace-content").load("/content/replace.html");
};

function clearLog(h) {

    var value = confirm("Are you sure?");
    if (value) {
        $.get("/", {
            type: "deleteLogs",
            value: h
        }).done(function(data) {

        });

        $("#badgeHooks").text("");
        $('#'+h+'1').load('?type=file&value='+h+'&count=-1');
        setCookie(h, "-2", 1);
    }
};

var line = "----------------------------------------Form Actions------------------------------------------------------";

function changeReturnType(obj) {

    if (obj.value == 'void') {
        $('#returnMatch').prop('disabled', true);
        $('#returnNewValue').prop('disabled', true);
        $('#returnMatch').prop('value', '');
        $('#returnNewValue').prop('value', '');
    } else {
        $('#returnMatch').prop('disabled', false);
        $('#returnNewValue').prop('disabled', false);
    }
};