//alert("working first");

// Your web app's Firebase configuration
var firebaseConfig = {
  apiKey: "AIzaSyDX--Zh-vHcxUZAlwzEvsA0wp6_HoGZNxo",
  authDomain: "iot-based-personal-health-card.firebaseapp.com",
  databaseURL: "https://iot-based-personal-health-card.firebaseio.com/",
  projectId: "iot-based-personal-health-card",
  storageBucket: "iot-based-personal-health-card.appspot.com",
  messagingSenderId: "555604254757",
  appId: "1:555604254757:web:2b2a475339a152be72dfcc",
};
// Initialize Firebase
firebase.initializeApp(firebaseConfig);

var pId;
$(document).ready(function(){
  console.log("ready");

  var database = firebase.database();
  var pTag;
  database.ref().on("value", function(snap){
    pTag = snap.child('RFID').val();
    pId = pTag;
    $("#patient_id").text(pTag);
    console.log("Utag: "+pTag);

    checkValid(pTag);  //check either tag is valid



  });

  function checkValid(pTag){
    firebase.database().ref().child("patients").child(pTag).on("value",function(snapshot){
      if(snapshot.exists()){
        //patient exist with this number
        showPatient(pTag);
      }
      else{
        $('#status_image').attr('src', 'images/unverified.png');

        //firebase.database().ref().child("verified").set("no");  //set overall verification status

        $('#message').text("Patient unverified!");
        $('#message').css('color','red');
        $("#progress").hide();
        $("#detail_btn").hide();
      }
    });
  }


  function showPatient(pTag){
    firebase.database().ref().child("patients").child(pTag).child("profile").on("value",function(snapshot){
      if(snapshot.exists()){

        var image = snapshot.child("image").val();
        var name = snapshot.child("name").val();
        var address = snapshot.child("address").val();
        var gender = snapshot.child("gender").val();
        var age = snapshot.child("age").val();
        var blood = snapshot.child("bloodGroup").val();

        console.log("image: "+image);

        $('#picture').attr('src', image);
        $('#patient_name').text(name);
        $('#patient_address').text(address);
        $('#patient_gender').text(gender);
        $('#patient_age').text(age);
        $('#patient_blood').text(blood);


        //show status
        $('#status_image').attr('src', 'images/verified.png');

        //firebase.database().ref().child("verified").set("yes");  //set overall verification status

        $('#message').text("Verified");
        $('#message').css('color','green');
        $("#progress").hide();
        $("#detail_btn").show();


      }

    });
  }



  $("#detail_btn").bind("click", function () {
    var url = "patient.html?pid=" + encodeURIComponent(pId);
    window.location.href = url;
  });



  //back button
    $("#back").click(function () {
        window.location.href = "index.html";
    });
});