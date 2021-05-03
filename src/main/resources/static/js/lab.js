var refUsers;
var refChat;
var chatroom;
var sendTo;
var riseHand = false;
var currentDocID;

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
    $('.steps ol li a').append("<span class=\"badge badge-secondary bg-secondary my-badge invisible\" onmouseover=\"hoverDiv(this,1)\" onmouseout=\"hoverDiv(this,0)\">0</span>")
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

    $("#btnSubmit").click(function (ev) {
        ev.preventDefault();
        $("#msg").html("")
        var db = firebase.firestore();
        db.collection("rooms").doc(getRoomID()).collection("submits").doc(currentUser.uid)
            .get()
            .then((doc) => {
                if (doc.exists) {
                    var url = "";
                    var obj = getSubmittedCurrentStep(getSelectedStep(), doc.data().steps);
                    if (obj != null) {
                        for (let i = 0; i < obj.fileNames.length; i++) {
                            url = url + "<a class='text-primary' href = '" + obj.fileLinks[i] + "' >" + obj.fileNames[i] + "</a ><br>";
                        }
                        $("#msg").html("<p>File đã nộp: <br>" + url);
                    }
                }
                $("#upload-spinner").addClass("d-none");
                $("#btn_upload").removeClass("d-none");
            })
            .catch((error) => {
                console.log("Error getting documents: ", error);
            });

        $("#uploadModal").modal("show");
    });

    $('#btn_upload').click(function () {
        $("#upload-spinner").removeClass("d-none");
        $("#upload-form").addClass("d-none");
        $("#btn_upload").addClass("d-none");
        var formData = new FormData($("#upload-form")[0]);
        formData.append("userID", currentUser.uid);
        formData.append("userName", currentUser.displayName);
        formData.append("step", getSelectedStep());
        formData.append("room", getRoomID());
        // AJAX request
        $.ajax({
            url: '/upload',
            type: 'post',
            data: formData,
            contentType: false,
            processData: false,
            success: function (response) {
                $("#upload-spinner").addClass("d-none");
                $("#upload-form").removeClass("d-none");
                $("#msg").html(response);
                $("#upload-form").trigger("reset");
                $("#btn_upload").removeClass("d-none");
            },
            error: function (response) {
                $("#msg").html("Có lỗi xảy ra!");
                $("#upload-spinner").addClass("d-none");
                $("#upload-form").removeClass("d-none");
                $("#upload-form").trigger("reset");
                $("#btn_upload").removeClass("d-none");
            }
        });
    });

    $("#btnReport").click(function (ev) {
        var db = firebase.firestore();
        var s;
        $("#tbody-report").html("")
        db.collection("rooms").doc(getRoomID()).collection("submits").onSnapshot((querySnapshot) => {
            $("#report-spinner").addClass("d-none");
            $("#table-report").removeClass("d-none");

            querySnapshot.forEach((doc) => { //Duyet tung nguoi dung
                var s = "";

                for (let i = 0; i < getNumberOfSteps(); i++) {
                    if (isSubmited(i, doc.data().steps)) {
                        s = s + "<span class ='labStep blue' >" + (i + 1) + "</span>";
                    } else {
                        s = s + "<span class ='labStep' >" + (i + 1) + "</span>";
                    }
                }

                $("#tbody-report").append("<tr><td>" + doc.data().userName + "</td><td>" + s + "</td></tr>")
            });

        });
        $("#reportModal").modal("show");
    });

    $(document).on('keydown', function (e) {
        if (e.keyCode === 37 || e.keyCode === 39) {
            var curStep = new URL(window.location.href).hash.split("#")[1];
            updateStep(Number(curStep))
        }
    });

});

function getSubmittedCurrentStep(step, arr) {
    for (const e of arr) {
        if (e.step === "" + step)
            return e;
    }
    return null;
}

function isSubmited(step, arr) {
    for (const e of arr) {
        if (e.step === "" + step)
            return true;
    }
    return false;
}

function enterRoom(user) {
    if (!getRoomID()) {
        $('#main').show();
        $('#drawer').show();
        $('#btnRoom').show();
    } else {
        //Ghi log vao storage
        var db = firebase.firestore();
        var roomRef = db.collection("rooms").doc(getRoomID());  //Doc thong tin cua Room
        roomRef.get().then((doc) => {
            if (doc.exists) {
                var obj = doc.data();
                currentDocID = obj.docID;
                if (obj.createdBy === user.uid) {
                    $("#btnReport").removeClass("d-none")

                } else {
                    $("#btnSubmit").removeClass("d-none")
                    $("#btnRiseHand").removeClass("d-none")
                }
            } else {
                // doc.data() will be undefined in this case
                console.log("No such document!");
            }
        }).catch((error) => {
            console.log("Error getting document:", error);
        });
        //Ghi Logs
        var userRef = roomRef.collection("logs").doc(currentUser.uid);
        userRef.set({
            lastEnter: firebase.firestore.FieldValue.serverTimestamp(),
            userName: user.displayName,
            userPhoto: user.photoURL
        })

        //Check realtime
        $('#main').show();
        $('#drawer').show();
        $('#btnRoom').show();
        refUsers = firebase.database().ref('/labs/' + currentDocID + '/' + getRoomID() + '/users');
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
                // if (currentUser.uid != uid) {  //Kh
                var avatar = "<img src=\"" + data[uid].photo + "\" alt=\"user\" width=\"40\" height=\"40\"  class=\"rounded-circle\">";
                if (!data[uid].photo && data[uid].name) {
                    var avatar = "<div><div class=\"friend\">" + data[uid].name + "</div></div>"
                }
                $('#usersChat').append("<a href='#' onclick='showChat(this,\"" + uid + "\")' class=\"list-group-item list-group-item-action rounded-0 media uchat\">" + avatar + "<div class=\"media-body\">" + data[uid].name + "</div></a>")
                if (!data[uid].photo && data[uid].name) {
                    $('.friend').nameBadge();
                }
                // }
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
        firebase.database().ref('/labs/' + currentDocID + '/' + getRoomID() + '/notifies/all').on('value', (snapshot) => {
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
        refUsers = firebase.database().ref('/labs/' + currentDocID + '/' + getRoomID() + '/users');
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
        refChat = firebase.database().ref('/labs/' + currentDocID + '/' + getRoomID() + '/chats/all/');
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
            ref = firebase.database().ref('/labs/' + currentDocID + '/' + getRoomID() + '/notifies/all')
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
    var steps = $(".steps ol li");
    return steps.length;
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
    //return "WmKeL3"; //TODO test
    // return (new URL(window.location.href)).searchParams.get('room')
    var arr = (new URL(window.location.href)).pathname.split("/");
    return arr[arr.length - 1]

}

