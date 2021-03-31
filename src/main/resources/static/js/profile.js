document.querySelector("#subscribeBtn").onclick = (e) => {
  e.preventDefault();
  document.querySelector(".modal-follow").style.display = "flex";
  // ajax 통신후에 json 리턴 -> javascript 오브젝트 변경 => for문 돌면서 뿌리기
  
  
  let userId = $("#userId").val();
  
  $.ajax({
  	type:"GET",
  	url:`/user/${userId}/follow`,
  	dataType:"json"
  }).done((res)=>{
  	console.log(res);
  	$("#follow_list").empty();
  	res.data.forEach((u)=>{
  		console.log(2,u);
  		let item = makeSubscribeInfo(u);
    	$("#follow_list").append(item);
  	});
  }).fail(error=>{
  	alert("오류 : "+error);
  });
  
};

function makeSubscribeInfo(u){
	let item=`<div class="follower__item" id="follow-${u.userId}">`;
	item +=`<div class="follower__img">`;
	item +=`<img src="/images/profile.jpeg" alt="">`;
	item +=`</div>`;
	item +=`<div class="follower__text">`;
	item +=`<h2>${u.username}</h2>`;
	item +=`</div>`;
	item +=`<div class="follower__btn">`;
	if(!u.equalState){
		if(u.followState){
			item +=`<button class="cta blue" onClick="followOrUnFollow(${u.userId})">구독취소</button>`;
		} else {
			item +=`<button class="cta" onClick="followOrUnFollow(${u.userId})">구독하기</button>`;
		}
	}
	item +=`</div>`;
	item +=`</div>`;
	
	return item;
}

function follow(userId){
	let text = $("#btnFollow").text();
	
	if(text === "구독취소"){
		$.ajax({
			type: "DELETE",
			url: "/follow/"+userId,
			dataType: "json"
		}).done(res=>{
			$("#btnFollow").text("구독하기");
			$("#btnFollow").toggleClass("blue");
		});
	}else{
		$.ajax({
			type: "POST",
			url: "/follow/"+userId,
			dataType: "json"
		}).done(res=>{
			$("#btnFollow").text("구독취소");
			$("#btnFollow").toggleClass("blue");
		});
	}

}

function followOrUnFollow(userId){
	let text = $(`#follow-${userId} button`).text();
	
	if(text === "구독취소"){
		$.ajax({
			type: "DELETE",
			url: "/follow/"+userId,
			dataType: "json"
		}).done(res=>{
			$(`#follow-${userId} button`).text("구독하기");
			$(`#follow-${userId} button`).toggleClass("blue");
		});
	}else{
		$.ajax({
			type: "POST",
			url: "/follow/"+userId,
			dataType: "json"
		}).done(res=>{
			$(`#follow-${userId} button`).text("구독취소");
			$(`#follow-${userId} button`).toggleClass("blue");
		});
	}
}


function closeFollow() {
  document.querySelector(".modal-follow").style.display = "none";
}
document.querySelector(".modal-follow").addEventListener("click", (e) => {
  if (e.target.tagName !== "BUTTON") {
    document.querySelector(".modal-follow").style.display = "none";
  }
});
function popup(obj) {
  console.log(obj);
  document.querySelector(obj).style.display = "flex";
}
function closePopup(obj) {
  console.log(2);
  document.querySelector(obj).style.display = "none";
}
document.querySelector(".modal-info").addEventListener("click", (e) => {
  if (e.target.tagName === "DIV") {
    document.querySelector(".modal-info").style.display = "none";
  }
});
document.querySelector(".modal-image").addEventListener("click", (e) => {
  if (e.target.tagName === "DIV") {
    document.querySelector(".modal-image").style.display = "none";
  }
});