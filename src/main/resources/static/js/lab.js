var refUsers;
var refChat;
var chatroom;
var sendTo;
var riseHand = false;

$(function () {
    page = "lab";
    $('.toast').toast()
    $('#codelab-feedback').hide();
    $("#topButton").detach().appendTo("#codelab-title");
    $("#creatroom").click(function () {
        $('#exampleModal').modal('show')
    });

    $("#done").hide();

    $('.steps ol li').click(function (e) {
        updateStep($(this).index());
    });

    var firstEnterRoom = true
    $('#btnRoom').click(function () {
        if (firstEnterRoom) {
            showChat($('#chat0'), "all")
            firstEnterRoom = false;
        }
    })

    $('#btnRiseHand').click(function () {
        riseHand = !$("#btnRiseHand").hasClass("active");
        var curStep = new URL(window.location.href).hash.split("#")[1];
        updateStep(Number(curStep))
    })
    $('.steps ol li a').append("<span class=\"badge badge-secondary bg-secondary my-badge invisible\" onmouseover=\"hoverDiv(this,1)\" onmouseout=\"hoverdiv(this,0)\">0</span>")
    $('#btnLogin').hide()
    if (getRoomID()) {  //Neu co phong thi an
        $('#main').hide();
        $('#drawer').hide();
        $('#btnLogin').show()
    }
    $("#next-step").click(function () {
        var curStep = new URL(window.location.href).hash.split("#")[1];
        updateStep(Number(curStep))
    });
    $("#previous-step").click(function () {
        var curStep = new URL(window.location.href).hash.split("#")[1];
        updateStep(Number(curStep))
    });
});

function enterLab(user) {
    if (!getRoomID()) {
        $('#main').show();
        $('#drawer').show();
        $('#btnRoom').show();
    } else {
        $('#main').show();
        $('#drawer').show();
        $('#btnRoom').show();
        refUsers = firebase.database().ref('/labs/' + getLabID() + '/' + getRoomID() + '/users');
        refUsers.on('value', (snapshot) => {
            const data = snapshot.val();
            var count = []
            var totalUser = 0;
            $('#usersChat').empty()
            var userinStep = "";
            for (var uid in data) {
                var step = data[uid].step;
                if (count[step] == undefined)
                    count[step] = {count: 0, user: ""};
                count[step].count++;
                count[step].user = count[step].user + data[uid].name + "<br>";
                totalUser++;
                //Add to chat room
                if (currentUser.uid != uid) {
                    var avatar = "<img src=\"" + data[uid].photo + "\" alt=\"user\" width=\"40\" height=\"40\"  class=\"rounded-circle\">";
                    if (!data[uid].photo && data[uid].name) {
                        var avatar = "<div><div class=\"friend\">" + data[uid].name + "</div></div>"
                    }
                    $('#usersChat').append("<a href='#' onclick='showChat(this,\"" + uid + "\")' class=\"list-group-item list-group-item-action rounded-0 media uchat\">" + avatar + "<div class=\"media-body\">" + data[uid].name + "</div></a>")
                    if (!data[uid].photo && data[uid].name) {
                        $('.friend').nameBadge();
                    }
                }
            }
            for (let i = 1; i <= getNumberOfSteps(); i++) {
                if (count[i - 1] == undefined)
                    $('li:nth-child(' + i + ') > a > span.badge').addClass("invisible")
                else {
                    $('li:nth-child(' + i + ') > a > span.badge').removeClass("invisible")
                    $('li:nth-child(' + i + ') > a > span.badge').text(count[i - 1].count);
                    $('li:nth-child(' + i + ') > a > span.badge').attr("user", count[i - 1].user)
                }
            }
            $('#numOnline').text(totalUser)
        });

        var leave = {};
        leave[currentUser.uid] = null;
        refUsers.onDisconnect().update(leave).then(function () {
            console.log("update exit")
        });
        updateStep(getSelectedStep());
        //Listen to Notification
        var first = true;
        firebase.database().ref('/notifies/' + currentUser.uid).on('value', (snapshot) => {
            if (!first) {
                const data = snapshot.val();
                if (!$("#collapse-online").hasClass("show") || ($("#collapse-online").hasClass("show") && sendTo !== data.uid)) {
                    $("#toastTitle").text(data.uname);
                    $("#toastBody").text(data.message);
                    $('.toast').toast('show');
                }
            }
            first = false;
        });

        var firstAll = true;
        firebase.database().ref('/labs/' + getLabID() + '/' + getRoomID() + '/notifies/all').on('value', (snapshot) => {
            if (!firstAll) {
                if (!$("#collapse-online").hasClass("show") || ($("#collapse-online").hasClass("show") && sendTo !== "all")) {
                    const data = snapshot.val();
                    $("#toastTitle").text("Chat room");
                    $("#toastBody").text(data.message);
                    $('.toast').toast('show');
                }
            }
            firstAll = false;
        });
        refUsers = firebase.database().ref('/labs/' + getLabID() + '/' + getRoomID() + '/users');
        var leave = {};
        leave[user.uid] = null;
        var enter = {};
        var curStep = new URL(window.location.href).hash.split("#")[1];
        if (!curStep)
            curStep = -1;

        enter[user.uid] = {
            step: curStep,
            time: firebase.database.ServerValue.TIMESTAMP,
            name: $("#profileName").text(),
            photo: user.photoURL
        };

        firebase.database().ref('.info/connected').on('value', function (snapshot) {
            if (snapshot.val() == false)
                return;
            refUsers.onDisconnect().update(leave).then(function () {
                refUsers.update(enter)
            });
        });
    }
}

function logoutRoom() {
    var leave = {};
    leave[currentUser.uid] = null;
    refUsers.update(leave);
    if (getRoomID()) {
        $('#main').hide();
        $('#drawer').hide();
        $('#btnRoom').hide();
    }
    if (refUsers)
        refUsers.off();
    if (refChat)
        refChat.off();
}

function showChat(me, uid) {
    sendTo = uid;
    if (refChat != null)
        refChat.off()
    $('#chatMessages').empty();
    if (uid === "all") {
        refChat = firebase.database().ref('/labs/' + getLabID() + '/' + getRoomID() + '/chats/all/');
        chatroom = uid;
    } else {
        if (uid > currentUser.uid)
            chatroom = uid + "-" + currentUser.uid;
        else
            chatroom = currentUser.uid + "-" + uid
        refChat = firebase.database().ref('/chats/' + chatroom);  //Private chat
    }
    refChat.on('child_added', (data) => {
        showMessage(data.val());
    });
    $(".uchat").removeClass("active text-white")
    $(me).addClass("active text-white")
}

function sendMessage() {
    if ($('#txtMessage').val().trim() !== "") {
        var change = {};
        change[refChat.push().key] = {
            uid: currentUser.uid,
            name: currentUser.displayName,
            photo: currentUser.photoURL,
            time: firebase.database.ServerValue.TIMESTAMP,
            message: $('#txtMessage').val()
        };
        refChat.update(change);
        var ref;
        if (sendTo === "all") {
            ref = firebase.database().ref('/labs/' + getLabID() + '/' + getRoomID() + '/notifies/all')
        } else {
            ref = firebase.database().ref('/notifies/' + sendTo)
        }
        ref.set({
            uid: currentUser.uid,
            uname: currentUser.displayName,
            message: $('#txtMessage').val(),
            time: firebase.database.ServerValue.TIMESTAMP
        });
        $('#txtMessage').val("")

        //REMOVE OLD CHAT
        const MAX_COUNT = 99;  //Keep 100 recent
        refChat.once('value', function (snapshot) {
            if (snapshot.numChildren() > MAX_COUNT) {
                var childCount = 0;
                var updates = {};
                snapshot.forEach(function (child) {
                    if (++childCount < snapshot.numChildren() - MAX_COUNT) {
                        updates[child.key] = null;
                    }
                });
                refChat.update(updates);
            }
        });
    }
}

function showMessage(data) {
    if (currentUser.uid === data.uid)
        $('#chatMessages').append("<div class=\"ml-auto d-flex justify-content-end\"><div class=\"chat-body\"><div class=\"bg-primary rounded-pill py-2 px-3  text-white text-small\">" + data.message + "</div><span class=\"text-muted d-flex justify-content-end chat-time\">" + time_ago(data.time) + "</span></div></div>\n")
    else {
        var avatar = "<img src=\"" + data.photo + "\" alt=\"user\" width=\"40\" height=\"40\"  class=\"rounded-circle\">";
        if (!data.photo && data.name) {
            var avatar = "<div><div class=\"friend\">" + data.name + "</div></div>"
        }
        $('#chatMessages').append("<div class=\"media w-75 \">" + avatar + "<div class=\"media-body ml-3\"><div class=\"bg-light rounded-pill py-2 px-3\"><span class=\"text-small mb-0 text-muted\">" + data.message + "</span></div><p class=\"text-muted chat-time\">" + time_ago(data.time) + "</p></div></div>");
        if (!data.photo && data.name) {
            $('.friend').nameBadge();
        }
    }
    var objDiv = document.getElementById("chatMessages");
    objDiv.scrollTop = objDiv.scrollHeight;
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
        change[currentUser.uid] = {
            step: step,
            time: firebase.database.ServerValue.TIMESTAMP,
            name: $("#profileName").text(),
            photo: currentUser.photoURL
        };

        if (riseHand) {
            change[currentUser.uid].isRise = true;
        }

        refUsers.update(change);
    }
}

function hoverDiv(e, state) {
    if (state === 1) {
        var left = 40 + $(e).offset().left + "px";
        var top = $(e).offset().top + "px";
        var div = document.getElementById('divtoshow');
        div.innerHTML = $(e).attr("user")
        div.style.left = left;
        div.style.top = top;
        $("#divtoshow").show();
    } else {
        $("#divtoshow").hide();
    }
    console.log(e)
}

function getRoomID() {
    return 1; //TODO test
    return (new URL(window.location.href)).searchParams.get('room')

}

function getLabID() {
    return 1; //TODO test
    var arr = (new URL(window.location.href)).pathname.split("/");
    return arr[arr.length - 1]
}

