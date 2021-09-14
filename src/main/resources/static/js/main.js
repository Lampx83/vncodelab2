var ui
var currentUser;
var availableTags = []
var insertLab = true;  //true mean insert false mean update
var oldCateID; // lưu old CateID lại để xóa đi sau khi sửa
var firebaseConfig = {
    apiKey: "AIzaSyCAowCDyHC5b0HhhIBvxVqc0o3lLSMXnJM",
    authDomain: "vncodelab2.firebaseapp.com",
    databaseURL: "https://vncodelab2-default-rtdb.firebaseio.com",
    projectId: "vncodelab2",
    storageBucket: "vncodelab2.appspot.com",
    messagingSenderId: "852532707206",
    appId: "1:852532707206:web:5281cd31d29828fbc7f607",
    measurementId: "G-6ZVL24X18C"
};
var uiConfig = {
    signInFlow: 'popup',
    signInOptions: [
        firebase.auth.EmailAuthProvider.PROVIDER_ID,
        firebase.auth.GoogleAuthProvider.PROVIDER_ID,
        firebase.auth.FacebookAuthProvider.PROVIDER_ID,
        "microsoft.com",
    ],
    callbacks: {
        signInSuccessWithAuthResult: function (authResult) {
            if (authResult.user) {
                afterLogin(authResult.user);
            }
            return false;
        },
        signInFailure: function (error) {

        }
    },
};

function afterLogin(user) {
    $(".user").removeClass("d-none")
    $(".guest").addClass("d-none")
    $('#loginModal').modal('hide')
    $("#name").text(user.displayName);
    $("#email").text(user.email);
    $("#phone").text(user.phoneNumber);

    if (user.photoURL) {
        $(".avatar").attr("src", user.photoURL);
    } else {
        $(".avatar").attr("src", "/images/user.svg");
    }

    $('#profileName').text(user.displayName)
    $('#profileEmail').text(user.email)
    if (hashCode(user.email) == "-448897477") {
        $(".lpx").removeClass("d-none")
    }
    if (page === "lab") {
        if (window.location.pathname.startsWith("/lab"))
            enterLab(user);
        if (window.location.pathname.startsWith("/room"))
            enterRoom(user);
    } else if (page === "mylabs")
        loadLabs(user);


}

function afterLogout() {
    ui.start('#firebaseui-auth-container', uiConfig);
    if (page === "mylabs")
        $("#cards").empty();
    $(".user").addClass("d-none")
    $(".guest").removeClass("d-none")
    if (window.location.pathname.startsWith("/room")) {
        $('#main').hide();
        $('#drawer').hide();
    } else if (window.location.pathname.startsWith("/lab"))
        enterLab();

    if (currentUser != null) {
        var userStatusDatabaseRef = firebase.database().ref('/status/' + currentUser.uid);
        var isOfflineForDatabase = {
            state: 'offline',
            last_changed: firebase.database.ServerValue.TIMESTAMP,
        };
        userStatusDatabaseRef.set(isOfflineForDatabase)
        if (page === "lab") {
            logoutRoom();
        }
    }

}

function hashCode(s) {
    return s.split("").reduce(function (a, b) {
        a = ((a << 5) - a) + b.charCodeAt(0);
        return a & a
    }, 0);
}

function createLabCard(lab, mylabs) {
    //Kiem tra xem da dua cate do len hay chua

    var hasCate = false;
    if (lab.cateID === "Khác") { //Làm vậy để khác luôn suất hiện ở cuối cùng
        $("#tab-other").removeClass("d-none")
        hasCate = true;
    } else {
        for (const element of $("#cate-tab").children()) {
            if ($(element).text() === lab.cateID.trim()) {
                hasCate = true;
                break;
            }
        }
        for (const element of $("#cate-tab-expand").children()) {
            if ($(element).text() === lab.cateID.trim()) {
                hasCate = true;
                break;
            }
        }
    }

    if (!hasCate) {
        // if ($("#cate-tab").children().length > 2)
        //     $("#tab-all").removeClass("d-none")
        // if ($("#cate-tab").children().length < 16)
        //     $("#tab-all").after("<a href='#' onclick='filterCate(\"" + lab.cateID.trim() + "\")' class='text-primary'>" + lab.cateID.trim() + "</a>")
        // else {
        $("#cate-tab-expand").prepend("<a href='#' class='dropdown-item' onclick='filterCate(\"" + lab.cateID.trim() + "\")' class='text-primary'>" + lab.cateID.trim() + "</a><span")
        //$("#cate-dropdown").removeClass("d-none")
        // }
        availableTags.push({"value": "- " + lab.cateID.trim() + " -", "id": lab.cateID})
    }
    availableTags.push({"value": lab.name.trim(), "id": lab.docID.trim()})


    // if (mylabs)
    //     return "<lab class = 'codelab-card codelab-card-item filter-cate-" + lab.cateID.trim() + "' id='" + lab.docID + "'>" +
    //         "<card class = 'codelab-card-inside " + ((lab.feature != null) ? 'card-feature' : '') + "'>" +
    //         "<card-header><h2 class='selectable'><a href = '/lab/" + lab.docID + "'>" + lab.name + "</a></h2><div><a href='#' class='bi bi-three-dots-vertical' data-toggle='dropdown'></a> <div class='dropdown-menu'><a class='dropdown-item' href='#' onclick='deleteLab(\"" + lab.docID + "\")'>Xóa</a><a class='dropdown-item' href='#' onclick='editLab(\"" + lab.docID + "\")'>Sửa</a> </div></div></card-header><h3 class='selectable'>" + lab.description + "</h3>" +
    //         "<div class='card-footer'>" +
    //         "<a class='text-primary align-middle text-uppercase' href='#' onclick='filterCate(\"" + lab.cateID.trim() + "\")'>" + lab.cateID + "</a>" +
    //         "<a href='#' onclick=\"loadRooms('" + lab.docID + "')\" type='button' class='btn btn-primary'>Phòng học</a>" +
    //         "</div></card></lab>";
    // else
    //     return "<lab class = 'codelab-card codelab-card-item filter-cate-" + lab.cateID.trim() + "' id='" + lab.docID + "'><card class = 'codelab-card-inside'><card-header><h2><a href = '/lab/" + lab.docID + "'>" + lab.name + "</a></h2></div></card-header><h3 class='selectable'>" + lab.description + "</h3><div class='card-footer'><a class='text-primary align-middle text-uppercase' href='#' onclick='filterCate(\"" + lab.cateID.trim() + "\")'>" + lab.cateID + "</a></div></card></lab>";


    if (mylabs)
        return "" +
            "<div class='col codelab-card-item  filter-cate-" + lab.cateID.trim() + "' id='" + lab.docID + "'>" +
            "   <div class = 'card " + ((lab.feature != null && lab.feature) ? 'card-feature' : 'card-not-feature') + "' >" +
            "           <div class='card-body'>" +
            "               <div class='d-flex'>" +
            "                  <a href = '/lab/" + lab.docID + "' class='flex-grow-1'> <span  class='card-title '>" + lab.name + "</span></a>" +
            "                   <a href='#' class='bi bi-three-dots-vertical' data-bs-toggle='dropdown'></a> " +
            "                       <div class='dropdown-menu'>" +
            "                           <a class='dropdown-item' href='#' onclick='deleteLab(\"" + lab.docID + "\")'>Xóa</a><a class='dropdown-item' href='#' onclick='editLab(\"" + lab.docID + "\")'>Sửa</a>" +
            "                       </div>" +
            "               </div>" +
            "               <p class='card-text selectable'>" + lab.description + "</p>" +
            "               <div class='d-flex'>" +
            "                   <a class='text-primary  align-self-center text-uppercase me-auto' href='#' onclick='filterCate(\"" + lab.cateID.trim() + "\")'>" + lab.cateID + "</a>" +
            "                   <a href='#' onclick=\"loadRooms('" + lab.docID + "')\" type='button' class='btn btn-primary'>Phòng học</a>" +
            "               </div>" +
            "           <div>" +
            "   </div>" +
            "</div>";
    else
        return "" +
            "<div class='col codelab-card-item filter-cate-" + lab.cateID.trim() + "'  id='" + lab.docID + "'>" +
            "   <div class = 'card'>" +
            "           <div class='card-body'>" +
            "               <a href = '/lab/" + lab.docID + "'><span  class='card-title'>" + lab.name + "</span></a>" +
            "               <p class='card-text selectable'>" + lab.description + "</p>" +
            "               <a class='text-primary align-middle text-uppercase' href='#' onclick='filterCate(\"" + lab.cateID.trim() + "\")'>" + lab.cateID + "</a>" +
            "           <div>" +
            "   </div>" +
            "</div>";
}

function filterCate(cateID) {
    if (cateID === "all") {
        $(".codelab-card-item").removeClass("d-none");
    } else {
        $(".codelab-card-item").addClass("d-none");
        $(".filter-cate-" + cateID).removeClass("d-none");
    }
    $("#cate-dropdown").removeClass("d-none");
    $("#cateID").val(cateID);
}

function loadLabs(user) {
    var db = firebase.firestore();
    db.collectionGroup("users").where("userID", "==", currentUser.uid).orderBy("order")
        .get()
        .then((querySnapshot) => {
            querySnapshot.forEach((doc) => {
                if (doc.exists) {
                    var description = doc.data().description;
                    var docRef = db.collection("labs").doc(doc.ref.parent.parent.id);
                    docRef.get().then((doc) => {
                        if (doc.exists) {
                            var lab = doc.data();
                            lab.description = description;
                            //$("#cards").append(createLabCard(lab, true));
                            $("#codelab-card-add").before(createLabCard(lab, true));
                        } else {
                            // doc.data() will be undefined in this case
                            console.log("No such document!");
                        }
                    }).catch((error) => {
                        console.log("Error getting document:", error);
                    });
                }
            });
            $(".codelab-card-add").removeClass("d-none")
            $("#spiner-loading-card").addClass("d-none")
        })
        .catch((error) => {
            console.log("Error getting documents: ", error);
        });
}

function loadFeatureLabs() {

    var db = firebase.firestore();
    let cateID = getUrlParameter("cateID");
    if (cateID)
        db.collection("labs").where("cateID", "==", cateID).where("feature", "==", true).orderBy("order").get().then((querySnapshot) => {
            loadLabByQuerySnapshot(querySnapshot);
        });
    else
        db.collection("labs").where("feature", "==", true).orderBy("order").get().then((querySnapshot) => {
            loadLabByQuerySnapshot(querySnapshot);
        });
}

function loadLabByQuerySnapshot(querySnapshot) {
    $("#cards").empty();
    querySnapshot.forEach((doc) => {
        if (doc.exists) {
            var lab = doc.data();
            $("#cards").append(createLabCard(lab, false));
        } else {
            console.log("No such document!");
        }
    });
    $(".codelab-card-add").removeClass("d-none")
    $("#spiner-loading-card").addClass("d-none")
}

function getUrlParameter(sParam) {
    var sPageURL = window.location.search.substring(1),
        sURLVariables = sPageURL.split('&'),
        sParameterName,
        i;

    for (i = 0; i < sURLVariables.length; i++) {
        sParameterName = sURLVariables[i].split('=');

        if (sParameterName[0] === sParam) {
            return typeof sParameterName[1] === undefined ? true : decodeURIComponent(sParameterName[1]);
        }
    }
    return false;
};

function loadRooms(docID) {
    $("rooms-spinner").removeClass("d-none");
    $("#addRoomModal").modal("show");
    $("#tbody-rooms").html("");
    firebase.firestore().collection("rooms").where("docID", "==", docID).get().then((querySnapshot) => {
        $("#table-rooms").removeClass("d-none");
        querySnapshot.forEach((doc) => {
            let room = doc.data();
            let row = "<tr id='" + room.roomID + "'><td class='align-middle'>" + room.roomID + "</td><td class='text-end align-middle'> <a href='/room/" + room.roomID + "' class='text-primary' target='_blank'>Vào phòng</a></td><td class='text-end align-middle'><a href='#' class='bi bi-three-dots-vertical' data-bs-toggle='dropdown'></a> <div class='dropdown-menu'><a class='dropdown-item' href='#' onclick='deleteRoom(\"" + room.roomID + "\")'>Xóa</a> </div></td></tr>";
            $("#tbody-rooms").append(row)
        });
        $("#rooms-spinner").addClass("d-none");
        if ($("#tbody-rooms").children().length == 0) {
            let row = "<tr><td class='align-middle'><i>Không có!</i></td></tr>";
            $("#tbody-rooms").append(row)
        }

    });
    $('#add-room-button').off('click');
    $("#add-room-button").click(function () {
        createRoom(docID);
    })
}

function deleteRoom(roomID) {
    $("#" + roomID).remove();
    var room = {}
    room["roomID"] = roomID;
    $.ajax({
        url: "/deleteRoom",
        type: "POST",
        data: JSON.stringify(room),
        dataType: "json",
        contentType: "application/json",
        success: function (response) {
            $(".toast").toast('show');
        },
        error: function (e) {
            console.log(e)
        }
    })
}

function createRoom(docID) {
    var room = {}
    room["docID"] = docID;
    room["userID"] = currentUser.uid;
    room["roomID"] = makeid(6);
    $.ajax({
        url: "/createRoom",
        type: "POST",
        data: JSON.stringify(room),
        dataType: "json",
        contentType: "application/json",
        success: function (response) {
            window.location.href = "/room/" + room["roomID"];
        },
        error: function (e) {
            console.log(e)
        }
    })
}

function showRegisterForm() {
    $('.loginBox').fadeOut('fast', function () {
        $('.registerBox').fadeIn('fast');
        $('.login-footer').fadeOut('fast', function () {
            $('.register-footer').fadeIn('fast');
        });
        $('.modal-title').html('Register with');
    });
    $('.error').removeClass('alert alert-danger').html('');
}

// function openLoginModal() {
//     $('#loginModal .registerBox').fadeOut('fast', function () {
//         $('.loginBox').fadeIn('fast');
//         $('.register-footer').fadeOut('fast', function () {
//             $('.login-footer').fadeIn('fast');
//         });
//
//         $('.modal-title').html('Đăng nhập với');
//     });
//     $('.error').removeClass('alert alert-danger').html('');
//     setTimeout(function () {
//         $('#loginModal').modal('show');
//     }, 230);
// }

// function loginAjax() {
//     var email = $("#email1").val();
//     var password = $("#password1").val();
//     firebase.auth().signInWithEmailAndPassword(email, password)
//         .then((userCredential) => {
//             var user = userCredential.user;
//             afterLogin(user)
//         })
//         .catch((error) => {
//             var errorMessage = error.message;
//             shakeModal();
//             $('.error').addClass('alert alert-danger').html(errorMessage);
//         });
// }

// function createAccount() {
//     var email = $("#email2").val();
//     var password = $("#password2").val();
//     firebase.auth().createUserWithEmailAndPassword(email, password)
//         .then((userCredential) => {
//             var user = userCredential.user;
//             user.updateProfile({
//                 displayName: $("#name").val()
//             }).then(function () {
//                 afterLogin(user)
//             })
//
//             // ...
//         })
//         .catch((error) => {
//             shakeModal();
//             var errorCode = error.code;
//             var errorMessage = error.message;
//             $('.error').addClass('alert alert-danger').html(errorMessage);
//         });
// }

// function shakeModal() {
//     $('#loginModal .modal-dialog').addClass('shake');
//     $('input[type="password"]').val('');
//     setTimeout(function () {
//         $('#loginModal .modal-dialog').removeClass('shake');
//     }, 1000);
// }

function presence(user) {
    let uid = user.uid;
    let userStatusDatabaseRef = firebase.database().ref('/status/' + uid);
    let isOfflineForDatabase = {
        state: 'offline',
        last_changed: firebase.database.ServerValue.TIMESTAMP,
        uname: user.displayName
    };
    let isOnlineForDatabase = {
        state: 'online',
        last_changed: firebase.database.ServerValue.TIMESTAMP,
        uname: user.displayName
    };
    firebase.database().ref('.info/connected').on('value', function (snapshot) {
        if (snapshot.val() == false)
            return;
        userStatusDatabaseRef.onDisconnect().set(isOfflineForDatabase).then(function () {
            userStatusDatabaseRef.set(isOnlineForDatabase);
        });
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

function time_ago(time1) {
    time = new Date(time1);
    return TimeAgo.inWords(time.getTime());
}

var TimeAgo = (function () {
    var self = {};
    // Public Methods
    self.locales = {
        prefix: '',
        sufix: '',
        seconds: 'just now',
        minute: 'about a minute',
        minutes: '%d minutes',
        hour: 'about an hour',
        hours: 'about %d hours',
        day: 'a day',
        days: '%d days',
        month: 'about a month',
        months: '%d months',
        year: 'about a year',
        years: '%d years'
    };

    self.inWords = function (timeAgo) {
        var seconds = Math.floor((new Date() - parseInt(timeAgo)) / 1000),
            separator = this.locales.separator || ' ',
            words = this.locales.prefix + separator,
            interval = 0,
            intervals = {
                year: seconds / 31536000,
                month: seconds / 2592000,
                day: seconds / 86400,
                hour: seconds / 3600,
                minute: seconds / 60
            };

        var distance = this.locales.seconds;

        for (var key in intervals) {
            interval = Math.floor(intervals[key]);

            if (interval > 1) {
                distance = this.locales[key + 's'];
                break;
            } else if (interval === 1) {
                distance = this.locales[key];
                break;
            }
        }

        distance = distance.replace(/%d/i, interval);
        words += distance + separator + this.locales.sufix;

        return words.trim();
    };

    return self;
}());

function createLab() {  //Thêm hoặc sửa Lab

    var valid = true;
    if ($("#docID").val().trim() === "" || (!$("#docID").val().includes("docs.google.com") && !$("#docID").val().includes("codelabs-preview.appspot.com") && $("#docID").val().length != 44)) {
        $("#docID").addClass("is-invalid")
        valid = false
    } else {
        $("#docID").removeClass("is-invalid")
        $("#docID").addClass("is-valid")
    }

    if ($("#cateID").val().trim() === "") {
        $("#cateID").addClass("is-invalid")
        valid = false
    } else {
        $("#cateID").removeClass("is-invalid");
        $("#cateID").addClass("is-valid")
    }

    if ($("#description").val().trim() === "") {
        $("#description").addClass("is-invalid")
        valid = false
    } else {
        $("#description").removeClass("is-invalid")
        $("#description").addClass("is-valid")
    }

    if (valid) {  //Mọi thứ hoàn chỉnh, OK
        var lab = {}
        lab["docID"] = $("#docID").val();
        lab["description"] = $("#description").val();
        lab["cateID"] = $("#cateID").val().trim();
        lab["userID"] = currentUser.uid;
        lab["feature"] = $("#featureCheckbox").is(':checked');
        lab["slides"] = $("#slideCheckbox").is(':checked');
        $('#modal-error').text("")
        $("#add-lab-button").html('');
        $("#add-lab-button").prop("disabled", true);
        $("#add-lab-button").html('<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Đang thực hiện...');
        var action
        if (insertLab)
            action = "insert"
        else {
            if ($("#updateCheckbox").is(':checked'))
                action = "updateAll"
            else
                action = "updateInfo"
        }
        $.ajax({  //Chuyển Request tới server
            url: "/createLab?action=" + action,
            type: "POST",
            data: JSON.stringify(lab),
            dataType: "json",
            contentType: "application/json",
            success: function (lab) {
                if (insertLab)  //nếu thêm bài lab mới thì thêm card
                    $("#codelab-card-add").before(createLabCard(lab, true));
                else { //Sửa bài lab thì cập nhật một số thông tin
                    $("#" + lab.docID + " " + "h3").text(lab.description)
                    $("#" + lab.docID + " " + ".cate-footer").text(lab.cateID)
                    if (oldCateID != lab.cateID.trim()) {
                        $("#" + lab.docID).removeClass("filter-cate-" + oldCateID)  //Xóa class cũ
                        $("#" + lab.docID).addClass("filter-cate-" + lab.cateID.trim())  //Them class mới
                        $("#" + lab.docID).addClass("d-none")
                    }
                }
                //Loading
                $("#add-lab-button").prop("disabled", false);
                $("#add-lab-button").html('Thêm');
                $('#addLabModal').modal('hide')
                $('#add-form').trigger("reset");
                $("#docID").removeClass("is-valid")
                $("#description").removeClass("is-valid")
                $("#cateID").removeClass("is-valid")
            },
            error: function (e) {
                $('#modal-error').text('Không thể thêm bài Lab')
                $("#add-lab-button").prop("disabled", false);
                $("#add-lab-button").html('Thêm');
            }
        })
    }
}

function editLab(docID) { //Hiển thị form sửa Lab
    insertLab = false;
    $('#updateCheckboxdiv').removeClass("d-none");
    //  $('#updateImageCheckboxdiv').removeClass("d-none");
    $('#addLabModal > div > div > div > h5').text("Sửa bài Lab")
    $('#docID').prop("disabled", true);
    $('#docID').val(docID)
    $('#add-lab-button').text("Cập nhật");
    $('#addLabModal').modal('show')
    var db = firebase.firestore();
    var docRef = db.collection("labs").doc(docID);
    docRef.get().then((doc) => {
        if (doc.exists) {
            $('#description').val(doc.data().description)
            $("#cateID").val(doc.data().cateID);
            if (doc.data().feature != null && doc.data().feature)
                $("#featureCheckbox").prop('checked', true);
            else
                $("#featureCheckbox").prop('checked', false);
            if (doc.data().slides != null && doc.data().slides)
                $("#slideCheckbox").prop('checked', true);
            else
                $("#slideCheckbox").prop('checked', false);

            oldCateID = doc.data().cateID.trim();
        } else {
            console.log("No such document!");
        }
    }).catch((error) => {
        console.log("Error getting document:", error);
    });
}

function deleteLab(docID) {
    $('#confirm-modal').modal('show')
    $('#confirm-title').text("Bạn có chắc chắn muốn xóa?")
    $('#confirm-body').text("Toàn bộ dữ liệu liên quan đến phòng thực hành sẽ bị xóa")

    $('#confirm-button').click(function () {
        var lab = {}
        lab["docID"] = docID;
        lab["userID"] = currentUser.uid;
        $("#confirm-button").prop("disabled", true);
        $("#confirm-button").html('<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Đang xóa...');
        $.ajax({
            url: "/deleteLab",
            type: "POST",
            data: JSON.stringify(lab),
            dataType: "json",
            contentType: "application/json",
            success: function (data) {
                $(".toast").toast('show');
                $('#confirm-modal').modal('hide')
                $('#' + docID).remove()
                $("#confirm-button").prop("disabled", false);
                $("#confirm-button").html('Xóa');
            },
            error: function (e) {
                $("#confirm-button").prop("disabled", false);
                $("#confirm-button").html('Xóa');

            }
        })
    })
}

$(function () {
    firebase.initializeApp(firebaseConfig);
    ui = new firebaseui.auth.AuthUI(firebase.auth());
    // ui.start('#firebaseui-auth-container', uiConfig);
    firebase.auth().onAuthStateChanged(function (user) {
        if (user) {  //Neu dang nhap roi
            currentUser = user;
            afterLogin(user);
            presence(user);
        } else { //Neu chua dang nhap
            if (((window.location.pathname.startsWith("/room")) || (window.location.pathname.startsWith("/mylabs")))) //Bat buoc phai dang nhap
                $('#loginModal').modal('show')  //Hien form dang nhap
            afterLogout();
        }
        $("#login-spinner").addClass("d-none")
    });

    $("#codelab-card-add").click(function (e) {
        $('#updateCheckboxdiv').addClass("d-none");
        // $('#updateImageCheckboxdiv').addClass("d-none");
        $('#addLabModal > div > div > div > h5').text("Thêm bài Lab")
        $('#docID').prop("disabled", false);
        $('#add-lab-button').text("Thêm");
        $('#docID').val("")
        $('#description').val("")
        $('#addLabModal').modal('show')
        insertLab = true;
        $("#updateCheckbox").prop('checked', false);
        // $("#updateImageCheckbox").prop('checked', false);
    });
    $("#add-form").submit(function (e) {
        e.preventDefault();
    });

    $('#input-search').keypress(function (e) {
        if (e.which == 13) {//
            loadFeatureLabs()
        }
    });


    if (page == "home" || page == "mylabs") {

        availableTags = []
        $("#input-search").autocomplete({
            source: availableTags,
            position: {
                my: "left-8 top+10"
            },
            select: function (e, ui) {
                if (ui.item.id.length == 44)
                    window.location.replace("/lab/" + ui.item.id);
                else
                    filterCate(ui.item.id)
            },
            autoFocus: true
        });


        $("#input-search").keydown(function (event) {
            if (event.keyCode == 13) {
                if ($(".selector").val().length == 0) {
                    event.preventDefault();
                    return false;
                }
            }
        });
        if (page === "mylabs") {
            $("#cate-dropdown").removeClass("d-none");
            // $("#cards").disableSelection();
            $("#cards").sortable({
                update: function (event, ui) {
                    var db = firebase.firestore();
                    for (let i = 0; i < $("#cards").children().length; i++) {
                        let id = $("#cards").children()[i].id
                        if (id !== "codelab-card-add" && !$("#" + id).hasClass("d-none")) {
                            db.collection("labs").doc(id).collection("users").doc(currentUser.uid).update({order: i});
                            db.collection("labs").doc(id).update({order: i});
                        }
                    }
                },
                cancel: ".selectable"
            });
        } else if (page === "home") {
            loadFeatureLabs()
        }
    }
});
