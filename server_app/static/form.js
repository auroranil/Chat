$(function() {
    // http://stackoverflow.com/a/22420377
    $.fn.serializeObject = function() {
        var o = {};
        var a = this.serializeArray();
        $.each(a, function() {
            if (o[this.name]) {
                if (!o[this.name].push) {
                    o[this.name] = [o[this.name]];
                }
                o[this.name].push(this.value || '');
            } else {
                o[this.name] = this.value || '';
            }
        });
        return o;
    };

    $("form").submit(function (e) {
        var that = this;    
        var formData = $(this).serializeObject();
        
        $.ajax({
            url: $(this).attr('action'),
            type: $(this).attr('method'),
            contentType: 'application/json',
            dataType : 'json',
            data: JSON.stringify(formData),
            success: function(data) {
                console.log($(that).attr('id'));
                switch($(that).attr('id')) {
                    case "form_login":
                        document.cookie = "session=" +  data["session"] + "; path=/";
                        window.location.href = "/";
                        break;
                    default:
                        console.log(data);
                }
            }
        });
        
        return false;
    });
});
