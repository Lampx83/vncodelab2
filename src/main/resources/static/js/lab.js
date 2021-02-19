var ref;


$(function () {

    $('#codelab-feedback').hide();
    $("#topButton").detach().appendTo("#codelab-title");

    $("#creatroom").click(function () {
        $('#exampleModal').modal('show')
    });
    $('#create-room-button').click(function (e) {
        createRoom("1EEGARIc9dEj9mpnmKoYP8n4EA9KNH9qR0W2c6CYEWT0", makeid(6));
    })
    $('.steps ol li').click(function (e) {
        updateStep($(this).index());
    });
// $('#add-lab-button').click(function (e) {
    //     var lab = {}
    //     lab["docID"] = $("#docID").val();
    //     lab["description"] = $("#description").val();
    //     lab["cateID"] = $("#cateID").val();
    //     $(this).prop("disabled", true);
    //     $(this).html('<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Adding...');
    //     $.ajax({
    //         url: "/save",
    //         type: "POST",
    //         data: JSON.stringify(lab),
    //         dataType: "json",
    //         contentType: "application/json",
    //         success: function (data) {
    //             $('#toast-title').text("Done")
    //             $('#toast-body').text("Lab has been added")
    //             $('#toast').toast('show')
    //             $('#exampleModal').modal('hide')
    //             $("#docID").text("")
    //             $("#description").text("")
    //             $('#add-lab-button').prop("disabled", false)
    //             $('#add-lab-button').html('Add')
    //         },
    //         error: function (e) {
    //             $('#add-lab-button').prop("disabled", false)
    //             $('#add-lab-button').html('Add')
    //             $('#modal-error').text('Please check your input!')
    //         }
    //     })
    // });

    $('.steps ol li a').append("<span class=\"badge badge-secondary bg-secondary my-badge invisible\" >0</span>")


    firebase.auth().onAuthStateChanged(function (user) {
        if (user) {
            currentUser = user;
            afterLogin(user);
            ref = firebase.database().ref('/labs/1EEGARIc9dEj9mpnmKoYP8n4EA9KNH9qR0W2c6CYEWT0/7OkmUq');

            ref.on('value', (snapshot) => {
                const data = snapshot.val();
                var currentStep = getSelectedStep();
                var count = []
                var totalUser = 0;
                $('#usersChat').empty()
                var uIndex = 0;
                for (var key in data.users) {
                    var step = data.users[key].step;
                    if (count[step] == undefined)
                        count[step] = 0;
                    count[step]++;
                    totalUser++;

                    $('#usersChat').append("<a href='#' onclick='showChat(this,\"" + key + "\")' class=\"list-group-item list-group-item-action rounded-0 media uchat\"><img src=\"" + data.users[key].photo + "\" alt=\"user\" width=\"40\" height=\"40\"  class=\"rounded-circle\"><div class=\"media-body\">" + data.users[key].name + "</div></a>")
                }
                for (let i = 1; i <= getNumberOfSteps(); i++) {
                    if (count[i - 1] == undefined)
                        $('li:nth-child(' + i + ') > a > span.badge').addClass("invisible")
                    else {
                        $('li:nth-child(' + i + ') > a > span.badge').removeClass("invisible")
                        $('li:nth-child(' + i + ') > a > span.badge').text(count[i - 1]);
                    }
                }

                $('#numOnline').text(totalUser)

            });
            var leave = {};
            leave['users/' + currentUser.uid] = null;
            ref.onDisconnect().update(leave);
            updateStep(getSelectedStep());
        } else {
            // No user is signed in.
        }
    });

});

function showChat(me, k) {
    $(".uchat").removeClass("active text-white")
    $(me).addClass("active text-white")

}

function getNumberOfSteps() {
    var radioButtons = $(".steps ol li");
    return radioButtons.length;
}

function getSelectedStep() {
    var radioButtons = $(".steps ol li");
    for (const element of radioButtons) {
        if (element.hasAttribute("selected"))
            return radioButtons.index(element)
    }
}

function updateStep(step) {
    if (currentUser != null) {
        var change = {};
        change['users/' + currentUser.uid] = {
            step: step,
            time: firebase.database.ServerValue.TIMESTAMP,
            name: currentUser.displayName,
            photo: currentUser.photoURL
        };
        ref.update(change);
    }
}

function createRoom(docID, roomID) {
    firebase.database().ref('labs/' + docID + "/" + roomID).set({
        create_time: firebase.database.ServerValue.TIMESTAMP
    });
}

function makeid(length) {
    var result = '';
    var characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    var charactersLength = characters.length;
    for (var i = 0; i < length; i++)
        result += characters.charAt(Math.floor(Math.random() * charactersLength));
    return result;
}

function writeLab(docID, room, user, step) {
    firebase.database().ref(docID + '/' + room).set({
        user: user,
        step: step,
    });
}

