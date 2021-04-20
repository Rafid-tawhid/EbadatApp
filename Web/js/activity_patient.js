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



var tabButtons=document.querySelectorAll(".tabContainer .buttonContainer button");
var tabPanels=document.querySelectorAll(".tabContainer  .tabPanel");

function showPanel(panelIndex,colorCode) {
    tabButtons.forEach(function(node){
        node.style.borderBottomColor="";
        node.style.color="";
    });
    //tabButtons[panelIndex].style.backgroundColor=colorCode;
    tabButtons[panelIndex].style.borderBottomWidth="3px";
    tabButtons[panelIndex].style.borderBottomColor=colorCode;
    tabButtons[panelIndex].style.color=colorCode;
    tabPanels.forEach(function(node){
        node.style.display="none";
    });
    tabPanels[panelIndex].style.display="block";
    //tabPanels[panelIndex].style.backgroundColor=colorCode;

}
showPanel(0,'#1FBFA9');



//functional code
var pId;
var dType;
var queryString = new Array();
$(document).ready(function(){
    if (queryString.length == 0) {             //get patientID from previous page
        if (window.location.search.split('?').length > 1) {
            var params = window.location.search.split('?')[1].split('&');
            for (var i = 0; i < params.length; i++) {
                var key = params[i].split('=')[0];
                var value = decodeURIComponent(params[i].split('=')[1]);
                queryString[key] = value;
            }
        }
    }

    //set patient ID
    console.log("getId: "+queryString["pid"]);
    if (queryString["pid"] != null) {
        var data = queryString["pid"];
        pId = data;
        $("#patient_id").text(data);
    }

    //set date
    firebase.database().ref().child("patients").child(data).child("lastUpdated").on("value",function(snapshot){
        if(snapshot.exists()){
            //patient exist with this number
            $("#last_update").text(snapshot.val());
        }
    });

    //profile-tab
    firebase.database().ref().child("patients").child(pId).child("profile").on("value",function(snapshot){
        if(snapshot.exists()){
            var image = snapshot.child("image").val();
            var name = snapshot.child("name").val();
            var address = snapshot.child("address").val();
            var gender = snapshot.child("gender").val();
            var age = snapshot.child("age").val();
            var blood = snapshot.child("bloodGroup").val();
            var email = snapshot.child("email").val();
            var qrImage = snapshot.child("qrImage").val();
            var homeNo = snapshot.child("homeNo").val();

            $('#picture').attr('src', image);

            $("#patient_name").text(name);
            $("#patient_mail").text(email);

            $('#qrImage').attr('src', qrImage);

            $("#patient_gender").text(gender);
            $("#patient_age").text(age);
            $("#patient_blood").text(blood);
            $("#patient_address").text(address);
            $("#patient_home").text(homeNo);

            $(".progress3").hide();
        }
    });


    //health tab
    var healthRef = firebase.database().ref().child("patients").child(pId).child("healthConditions");
    healthRef.on("value",function(snapshot){
        var allHealthsHTML = "";
        snapshot.forEach(function (childSnapshot) {
            var health = childSnapshot.val();
            console.log(health); // console to see it!
            var nextHealthHTML = `<div class='card' id='h_field'><div class='card-body' id='card_content'>
<span id='d_name'>`+health.deasesName+`</span></br>
<span class = "key">Reort type: </span><span class = "value">`+health.reportType+`</span></br>
<span class = "key">Result value: </span><span class = "value">`+health.labResult+`</span></br>
<span class = "key">Health condition: </span><span class = "value">`+health.condition+`</span></br>
<span class = "key">Examine date: </span><span class = "value">`+health.examineDate+`</span></br>
</div></div>`;
            allHealthsHTML += nextHealthHTML;

        });

        $('#health_list').html(allHealthsHTML);
        $(".progress1").hide();

    });





    //document tab
    var documentRef = firebase.database().ref().child("patients").child(pId).child("documents");
    documentRef.on("value",function(snapshot){
        var allDocumentsHTML = "";
        snapshot.forEach(function (childSnapshot) {
            var document = childSnapshot.val();
            console.log(document); // console to see it!

            dType = document.reportType;

            var nextDocumentHTML = `<div class='card' id='d_field'><div class='card-body' id='card_content'>
<a href='`+document.reportImage+`' data-lightbox='mygallery' data-title=`+dType+`><img src = "`+document.reportImage+`" class='h_image'></a></br>
<div id = 'bottom'>
<span id='r_type'>`+document.reportType+`</span></br>
<span class = "key">Generate Date: </span><span class = "value">`+document.generateDate+`</span></br>
</div>
</div></div></br>`;
            allDocumentsHTML += nextDocumentHTML;

        });

        $('#document_list').html(allDocumentsHTML);
        $(".progress2").hide();
    });



    //prescription tab
    var prescriptionRef = firebase.database().ref().child("patients").child(pId).child("prescriptions");
    prescriptionRef.on("value",function(snapshot){
        var allPrescriptionsHTML = "";
        snapshot.forEach(function (childSnapshot) {
            var prescription = childSnapshot.val();
            console.log(prescription); // console to see it!

            doctorName = prescription.doctorName;

            var nextprescriptionHTML = `<div class='card' id='d_field'><div class='card-body' id='card_content'>
<a href='`+prescription.prescriptionImage+`' data-lightbox='mygallery' data-title=`+doctorName+`><img src = "`+prescription.prescriptionImage+`" class='h_image'></a></br>
<div id = 'bottom'>

<span class = "key">Doctor Name: </span><span class = "value">`+doctorName+`</span></br>
<span class = "key">Speciality: </span><span class = "value">`+prescription.doctorDesignation+`</span></br>
<span class = "key">Prescribed Date: </span><span class = "value">`+prescription.prescribeDate+`</span></br>
</div>
</div></div></br>`;
            allPrescriptionsHTML += nextprescriptionHTML;

        });

        $('#prescription_list').html(allPrescriptionsHTML);
        $(".progress2").hide();
    });




    //back button
    $("#back").click(function () {
        window.location.href = "auth.html";
    });
});