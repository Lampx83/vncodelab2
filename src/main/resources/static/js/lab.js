var refUsers;
var refChat;
var chatroom;
var sendTo;
var raiseHand = false;
var currentDocID;
const HAND_UP = 0;
const HAND_DOWN = 1;
var teacher = false;
var firstReport = true;

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
    $('#btnRaiseHand').click(function () {
        raiseHand = !$("#btnRaiseHand").hasClass("active");
        var curStep = new URL(window.location.href).hash.split("#")[1];
        updateStep(Number(curStep))

        //Ghi log vao storageblue
        var db = firebase.firestore();
        var userRef = db.collection("rooms").doc(getRoomID()).collection("logs").doc(currentUser.uid).collection("hands")
        if (raiseHand) {
            userRef.add({
                time: firebase.firestore.FieldValue.serverTimestamp(),
                type: HAND_UP,
                step: Number(curStep)
            })
        } else {
            userRef.add({
                time: firebase.firestore.FieldValue.serverTimestamp(),
                type: HAND_DOWN,
                step: Number(curStep)
            })
        }

    })
    $('.steps ol li a').append("<span class=\"badge badge-secondary bg-secondary my-badge invisible\" onmouseover=\"hoverDiv(this,1)\" onmouseout=\"hoverDiv(this,0)\">0</span>")
    $('#btnLogin').hide()
    // if (getRoomID()) {  //Neu co phong thi an
    //
    // }
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
                    if (doc.data().steps != null) {
                        var obj = getSubmittedCurrentStep(getSelectedStep(), doc.data().steps);
                        if (obj != null) {
                            for (let i = 0; i < obj.fileNames.length; i++) {
                                url = url + "<a class='text-primary' href = '" + obj.fileLinks[i] + "' >" + obj.fileNames[i] + "</a ><br>";
                            }
                            $("#msg").html("<p>File đã nộp: <br>" + url);
                        }
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
        if (firstReport) {
            $("#raisehand-tab").click();
            firstReport = false;
        } else {
            //Reload
            $("#" + $('.nav-tabs .active').attr("id")).click();
        }
    });

    $("#raisehand-tab").click(function (ev) {
        $("#tbody-report-raisehand").html("")
        var room = {}
        room["roomID"] = getRoomID();
        room["numberOfStep"] = getNumberOfSteps();

        $.ajax({
            url: '/report_raisehand',
            type: 'post',
            data: JSON.stringify(room),
            dataType: "json",
            contentType: "application/json",
            success: function (response) {
                refUsers.get().then((snapshot) => {
                    if (snapshot.exists()) {
                        const data = snapshot.val();
                        for (var uid in data) {
                            var step = data[uid].step;
                            $('[id^=' + uid + ']').removeClass("yellow")
                            if (data[uid].isRaise) {
                                $("#" + uid + "_" + step).addClass("yellow")
                            }
                        }
                    }
                }).catch((error) => {
                    console.error(error);
                });
                $("#raisehand-spinner").addClass("d-none");
                $("#table-report-raisehand").removeClass("d-none");
                $("#tbody-report-raisehand").html(response.msg);
                if ($('#switch-showdetail').is(':checked')) {
                    $(".report-detail").removeClass("d-none")
                } else {
                    $(".report-detail").addClass("d-none")
                }
            },
            error: function (response) {
                $("#raisehand-spinner").addClass("d-none");
                $("#table-report-raisehand").removeClass("d-none");
                $("#tbody-report-raisehand").html(response.msg)
            }
        });
    });

    $("#practice-tab").click(function (ev) {
        $("#tbody-report-practice").html("")
        var room = {}
        room["roomID"] = getRoomID();
        room["numberOfStep"] = getNumberOfSteps();

        $.ajax({
            url: '/report_practice',
            type: 'post',
            data: JSON.stringify(room),
            dataType: "json",
            contentType: "application/json",
            success: function (response) {
                refUsers.get().then((snapshot) => {
                    if (snapshot.exists()) {
                        const data = snapshot.val();
                        for (var uid in data) {
                            var step = data[uid].step;
                            $('[id^=' + uid + ']').removeClass("yellow")
                            if (data[uid].isRaise) {
                                $("#" + uid + "_" + step).addClass("yellow")
                            }
                        }
                    }
                }).catch((error) => {
                    console.error(error);
                });
                $("#practice-spinner").addClass("d-none");
                $("#table-report-practice").removeClass("d-none");
                $("#tbody-report-practice").html(response.msg);
                if ($('#switch-showdetail').is(':checked')) {
                    $(".report-detail").removeClass("d-none")
                } else {
                    $(".report-detail").addClass("d-none")
                }
            },
            error: function (response) {
                $("#practice-spinner").addClass("d-none");
                $("#table-report-practice").removeClass("d-none");
                $("#tbody-report-practice").html(response.msg)
            }
        });
    });

    $("#submit-tab").click(function (ev) {
        var db = firebase.firestore();
        $("#tbody-report-submit").html("")
        db.collection("rooms").doc(getRoomID()).collection("submits").onSnapshot((querySnapshot) => {
            $("#submit-spinner").addClass("d-none");
            $("#table-report-submit").removeClass("d-none");
            $("#tbody-report-submit").html("")
            querySnapshot.forEach((doc) => { //Duyet tung nguoi dung
                var s = "";
                for (let i = 0; i < getNumberOfSteps(); i++) {
                    if (doc.data().steps != null) {
                        var submitedObjects = getSubmitedObjects(i, doc.data().steps);
                        if (submitedObjects != null) {
                            var link = "";
                            for (let j = 0; j < submitedObjects.fileNames.length; j++) {
                                let l = submitedObjects.fileNames[j]
                                link = link + "[<a class='text-primary' href='" + submitedObjects.fileLinks[j] + "'>" + l + "</a>] "
                            }
                            s = s + "<td><span class ='labStep blue'>" + (i + 1) + "</span><span class='hideSmall report-detail d-none'>" + link + "</span></td>";
                        } else {
                            s = s + "<td><span class ='labStep' >" + (i + 1) + "</span></td>";
                        }
                    } else {
                        s = s + "<td><span class ='labStep' >" + (i + 1) + "</span></td>";
                    }
                }
                let tdThreeDots = "<td class='text-right align-middle'><a href='#' class='bi bi-three-dots-vertical' data-toggle='dropdown'></a> <div class='dropdown-menu'><a class='dropdown-item' href='#' onclick='deleteUserReport(\"" + doc.id + "\")'>Xóa</a> </div></td>";

                $("#tbody-report-submit").append("<tr  id='tr-report-" + doc.id + "'><td>" + doc.data().userName + "</td>" + s + tdThreeDots + "</tr>")
                if ($('#switch-showdetail').is(':checked')) {
                    $(".report-detail").removeClass("d-none")
                } else {
                    $(".report-detail").addClass("d-none")
                }
            });
        });
    });


    $('#switch-showdetail').click(function (e) {
        if ($('#switch-showdetail').is(':checked')) {
            $(".report-detail").removeClass("d-none")
        } else {
            $(".report-detail").addClass("d-none")
        }
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

function getSubmitedObjects(step, arr) {
    for (const e of arr) {
        if (e.step === "" + step)
            return e;
    }
    return null;
}

function enterLab() {
    $('#main').show();
    $('#drawer').show();
    $('#btnLogin').hide()
}

function enterRoom(user) {
    $("#btnRoom").removeClass("d-none")
    $('#main').show();
    $('#drawer').show();
    $('#btnLogin').hide()

    var db = firebase.firestore();
    var roomRef = db.collection("rooms").doc(getRoomID());  //Doc thong tin cua Room
    roomRef.get().then((doc) => {
        if (doc.exists) {
            var obj = doc.data();
            currentDocID = obj.docID;
            if (obj.userID === user.uid) {
                $("#btnReport").removeClass("d-none")  //Teacher
                teacher = true;
            } else {
                $("#btnSubmit").removeClass("d-none")
                $("#btnRaiseHand").removeClass("d-none")
            }
            realtime(user);
        } else {
            // doc.data() will be undefined in this case
            console.log("No such document!");
        }
    }).catch((error) => {
        console.log("Error getting document:", error);
    });
    //Ghi log vao firestore
    var userRef = roomRef.collection("logs").doc(currentUser.uid);
    userRef.set({
        lastEnter: firebase.firestore.FieldValue.serverTimestamp(),
        userName: user.displayName,
        userPhoto: user.photoURL
    })

    var userRef = roomRef.collection("submits").doc(currentUser.uid);
    userRef.get().then((docSnapshot) => {
        if (!docSnapshot.exists) {
            userRef.set({
                userName: user.displayName
            })
        }
    });
}

function realtime(user) {
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

            if (teacher) {  //For Teacher only
                $('[id^=' + uid + ']').removeClass("yellow")
                if (data[uid].isRaise) {
                    $("#" + uid + "_" + step).addClass("yellow")
                }
            }

            //Add to chat room
            // if (currentUser.uid != uid) {  //Kh
            var avatar = "<img src='" + data[uid].photo + "' alt='user' width='40' height='40'  class='rounded-circle'>";
            if (!data[uid].photo && data[uid].name) {
                var avatar = "<div><div class='friend'>" + data[uid].name + "</div></div>"
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
    // refUsers = firebase.database().ref('/labs/' + currentDocID + '/' + getRoomID() + '/users');
    var leave = {};
    leave[user.uid] = null;
    var enter = {};
    var curStep = new URL(window.location.href).hash.split("#")[1];
    if (!curStep)
        curStep = -1;

    enter[user.uid] = {
        step: curStep,
        time: firebase.database.ServerValue.TIMESTAMP,
        name: $("#name").text(),
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

function logoutRoom() {
    var leave = {};
    leave[currentUser.uid] = null;
    refUsers.update(leave);
    $('#main').hide();
    $('#drawer').hide();
    $('#btnRoom').hide();
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
        $('#chatMessages').append("<div class=\"ml-auto d-flex justify-content-end\"><div class=\"pt-2 chat-body\"><div class=\"bg-primary rounded-pill py-2 px-3  text-white text-small\">" + data.message + "</div><span class=\"text-muted d-flex justify-content-end chat-time\">" + time_ago(data.time) + "</span></div></div>\n")
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

var oldStep = -1;
var oldTime = 0;

function updateStep(step) {
    var db = firebase.firestore();
    var newTime = Math.floor(Date.now() / 1000);
    var duration = newTime - oldTime;

    if (duration > 15 && duration < 1800) {
        var userRef = db.collection("rooms").doc(getRoomID()).collection("logs").doc(currentUser.uid).collection("steps")
        userRef.add({
            time: firebase.firestore.FieldValue.serverTimestamp(),
            enter: step,
            leave: oldStep,
            duration: duration
        })
    }

    if (currentUser != null) {
        var change = {};
        change[currentUser.uid] = {
            step: step,
            time: firebase.database.ServerValue.TIMESTAMP,
            name: $("#name").text(),
            photo: currentUser.photoURL
        };

        if (raiseHand) {
            change[currentUser.uid].isRaise = true;
        }

        refUsers.update(change);
    }
    oldStep = step
    oldTime = Math.floor(Date.now() / 1000);
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
}

function getRoomID() {
    // return "WmKeL3"; //TODO test
    // return (new URL(window.location.href)).searchParams.get('room')
    var arr = (new URL(window.location.href)).pathname.split("/");
    return arr[arr.length - 1]
}

function deleteUserReport(userID) {  //Hàm này có dùng nhé không được xóa
    var room = {}
    $("#tr-report-" + userID).remove();
    room["roomID"] = getRoomID();
    room["userID"] = userID;
    $.ajax({
        url: "/deleteUserReport",
        type: "POST",
        data: JSON.stringify(room),
        dataType: "json",
        contentType: "application/json",
        success: function (data) {

        },
        error: function (e) {


        }
    })
}

