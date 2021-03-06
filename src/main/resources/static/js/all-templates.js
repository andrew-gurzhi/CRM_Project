$(document).ready(RenderTableOfTemplates());

function RenderTableOfTemplates() {
    var trHTML = '';
    let element = $('#table-body');
    $.ajax({
        url: '/rest/message-template/',
        type: 'GET',
        async: true,
        success: function (data) {
            for (let i = 0; i < data.length; i++) {
                trHTML += "<tr>" +
                    "<td>" + data[i].id + "</td>" +
                    "<td>" + data[i].name + "</td>" +
                    "<td class='fit' align='right'>" +
                        "<button class='btn btn-primary glyphicon glyphicon-font' data='" + data[i].id + "' value='" + data[i].name +
                            "' onclick='renameTemplate(this)' title='Переименовать шаблон'></button>" +
                        "<button class='btn btn-primary glyphicon glyphicon-font glyphicon-text-size' data='" + data[i].id +
                            "' value='" + data[i].theme + "' onclick='renameTemplateTheme(this)' " +
                            "title='Переименовать тему шаблона'></button>" +
                        "<button class='button_edit_template btn btn-info glyphicon glyphicon-pencil' value='" + data[i].id +
                            "' title='Изменить шаблон'></button>" +
                        "<button class='button_delete_template btn btn-danger glyphicon glyphicon-remove' value='" + data[i].id +
                            "' title='Удалить шаблон'></button>" +
                    "</td>" +
                    "</tr>";
            }
            element.empty();
            element.append(trHTML);
        },
        error: function (jqXHR) {
            if (jqXHR.status == "404") {
                trHTML +=
                    "<tr>" +
                        "<td></td>" +
                        "<td>В базе нет ни одного шаблона!</td>" +
                        "<td class='fit' align='right'></td>" +
                    "</tr>";
                element.empty;
                element.append(trHTML);
            }
        }
    });
}

//Open create-new-template modal
$("#button_create_template").click(function () {
    $('#template-create-modal').modal('show');
});

//Create new template
$("#create_template").click(function () {
    let name = $("#template-name").val();
    let pattern = /^(?!\s*$).+/;
    if (pattern.test(name)) {
        $.ajax({
            type: 'POST',
            url: '/rest/message-template',
            dataType: "JSON",
            data: {name: name},
            success: function () {
                window.location = "/template/create/" + name;
            },
            error: function () {
                alert("Шаблон с таким именем " + name + " уже существует!");

            }
        });
    } else {
        $("#template-create-modal-err").html("Имя шаблона не может быть пустым!");
    }
});

//Edit template page redirect
$("#table-body").on('click', '.button_edit_template', function () {
    let id = this.value;
    window.location = "/template/edit/" + id;
});

//Delete template modal button
$("#table-body").on('click', '.button_delete_template', function () {
    if(!confirm("Вы уверены, что хотите удалить запись?")) {return}
    let id = this.value;
    $.ajax({
        type: 'POST',
        url: '/rest/message-template/delete',
        data: {id: id},
        success: function (response) {
            if (response === "CONFLICT") {
                alert("Шаблон используется для оповещения!");
            } else {
                RenderTableOfTemplates();
            }
        }
    });
});

//Clearing text inside #template-create-modal-err after closing modal
$('#template-create-modal').on('hidden.bs.modal', function () {
    $("#template-create-modal-err").empty();
});

//Clearing text inside #template-create-modal-err on input
$('#template-name').on('input', function () {
    $("#template-create-modal-err").empty();
});

//Clearing text inside #rename-template-modal-err on input
$('#template-rename').on('input', function () {
    $("#rename-template-modal-err").empty();
});

//Clearing text inside #rename-theme-template-modal-err on input
$('#template-theme-rename').on('input', function () {
    $("#rename-theme-template-modal-err").empty();
});

//Rename template
function renameTemplate(button) {
    //Get id and name
    var id = button.getAttribute('data');
    var oldName = button.value;
    //Set name field
    document.getElementById('template-rename').value = oldName;
    //Clean error row
    $("#rename-template-modal-err").empty();
    //Show modal
    $('#rename-template-modal').modal('show');
    //Save new name, remove old click listeners before
    $("#send_name").off('click').click(function () {
        //Get new name
        var newName = $('#template-rename').val();
        var pattern = /^(?!\s*$).+/;
        //Check name
        if (oldName === newName) {
            //Hide modal
            $('#rename-template-modal').modal('hide');
        } else if (pattern.test(newName)) {
            var data = {
                id: id,
                name: newName
            };
            $.ajax({
                type: 'POST',
                url: '/rest/message-template/rename',
                data: data,
                success: function (response) {
                    if (response === "BAD_REQUEST") {
                        alert("Такое имя уже используется!");
                    } else {
                        RenderTableOfTemplates();
                        $('#rename-template-modal').modal('hide');
                    }
                }
            });
        } else {
            $("#rename-template-modal-err").html("Имя шаблона не может быть пустым!");
        }
    });
};

//Rename template theme
function renameTemplateTheme(button) {
    //Get id and name
    var id = button.getAttribute('data');
    var oldThemeName = button.value;
    //Set name field
    document.getElementById('template-theme-rename').value = oldThemeName;
    //Clean error row
    $("#rename-theme-template-modal-err").empty();
    //Show modal
    $('#rename-theme-template-modal').modal('show');
    //Save new name, remove old click listeners before
    $("#send_theme").off('click').click(function () {
        //Get new name
        var newThemeName = $('#template-theme-rename').val();
        var pattern = /^(?!\s*$).+/;
        //Check name
        if (oldThemeName === newThemeName) {
            //Hide modal
            $('#rename-theme-template-modal').modal('hide');
        } else if (pattern.test(newThemeName)) {
            var data = {
                id: id,
                theme: newThemeName
            };
            $.ajax({
                type: 'POST',
                url: '/rest/message-template/renameTheme',
                data: data,
                success: function (response) {
                    if (response === "BAD_REQUEST") {
                        alert("Такая тема уже используется!");
                    } else {
                        RenderTableOfTemplates();
                        $('#rename-theme-template-modal').modal('hide');
                    }
                }
            });
        } else {
            $("#rename-theme-template-modal-err").html("Тема шаблона не может быть пустой!");
        }
    });
};