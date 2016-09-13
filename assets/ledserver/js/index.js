$(document).ready (function () {
	
	var r=255,g=255,b=255;
	$("#rgbBox").hide();						 
	$("#colorTempAndRgb").change(function(){ 						 
		var v=$(this).children('option:selected').val();// 					 
		if(v==1){
			$("#colorTempBox").show();
			$("#rgbBox").hide();
		}else if(v==2){
			$("#colorTempBox").hide();
			$("#rgbBox").show();
		}					 
	})
	$("#onOff").change(function(){ 						 
		var value=$(this).children('option:selected').val();// 
		$.ajax({
			cache:false,
			type:"POST",
			url:"/index",
			data:{
				onoff:value
			},
			async:true
		});

		//window.location.href="index?onoff="+value+"";		 
	})						 
							 
	$("#testMode").change(function(){
		var value=$(this).children('option:selected').val();// 
		$.ajax({
			cache:false,
			type:"POST",
			url:"/index",
			data:{
				testmode:value
			},
			async:true
		});
	})						 
							 
	$("#programMan").change(function(){
		var path=$(this).children('option:selected').val();// 
		var fileName=$(this).children('option:selected').html();//
		
		$.ajax({
			cache:false,
			type:"POST",
			url:"/index",
			data:{
				path:path,
				file_name:fileName
				
			},
			async:true
		});
	
	})							 
	$("#languageChange").change(function(){
		var value=$(this).children('option:selected').val();// 
		$.ajax({
			cache:false,
			type:"POST",
			url:"/index",
			data:{
				language:value
			},
			async:true
		});
	
	})									 
							 
							 
							 
	var brightSlider=new Slider();
	var colorTempSlider=new Slider();
	brightSlider.slider("brightBg","brightBar","brightInfo",255,0);
	colorTempSlider.slider("colorTempBg","colorTempBar","colorTempInfo",10000,2000);
	
	//get DOM
 	function S(id){
		return $("#"+id);
	}
	// get RGB value
	function cal(per){
		
		var per=parseInt(per);
		
		if(per==0){
			return 0;
		}	 
		return parseInt(parseInt(per)*255/100);
	}



	//Post Request
	 var httpAdapter;
	 function GethttpAdapterRequest()
    {
        
         return window.ActiveXObject ? new ActiveXObject("Microsoft.XMLHTTP") : new XMLHttpRequest();
    }
	function GetData(url){
		 httpAdapter = GethttpAdapterRequest();
		   httpAdapter.Open("GET",url, false);
		   httpAdapter.SetRequestHeader ("Content-Type","text/xml; charset=utf-8");
		//   httpAdapter.SetRequestHeader ("SOAPAction","http://tempuri.org/getStr");
		   httpAdapter.Send(context);
		   return httpAdapter;	
	}
	function PostData(url,context){
		httpAdapter = GethttpAdapterRequest();
        httpAdapter.Open("POST",url, false);
   		httpAdapter.setRequestHeader("Content-Type","application/x-www-form-urlencoded");
// 	  httpAdapter.SetRequestHeader ("SOAPAction","http://tempuri.org/getStr");
		httpAdapter.Send(context);
	   	return httpAdapter;	
	}
	
	//class
			function Slider() {

				var maxNum;
				var isDown = false;
				var isMoveing = false;

				var X;
				var offsetX;
				var parentW;
				var width;
				var num = 0;
				var maxValue = 255;
				var minValue = 0;
				var left = 0;
				var percent = 0;
				this.slider = function(bg, bar, info, maxValue, minValue) {

					S(bg).mousemove(
							function(e) {

								if (!this.isDown) {
									return;

								}
								this.maxValue = maxValue;

								this.minValue = minValue;

								this.X = S(bg).offset().left;

								this.offsetX = S(bar).width() / 2;

								this.parentW = S(bg).width();

								this.width = S(bar).width();
								// alert("hhh");
								this.isMoveing = true;
								// alert("X:"+X);
								var mouseX = e.pageX;
								// alert(mouseX);
								var x = S(bar).offset().left;
								// alert(x);
								var x0 = mouseX - x;
								// alert(x0);
								this.left = mouseX - this.X - this.offsetX;

								if (this.left <= 0) {
									this.left = 0;
									this.num = 0;
								}
								var maxLeft = this.parentW - this.width;
								// alert(this.width);
								if (this.left >= maxLeft) {
									this.left = maxLeft;
									this.num = maxValue;
								}
								S(bar).css("left", this.left + "px");

								this.percent = this.left * 100 / (maxLeft);

								this.num = parseInt(this.left
										* (maxValue - minValue) / maxLeft
										+ minValue);
								if (this.minValue > 0) {
									// alert(this.num);
									S(info).html(this.num + "K");
								} else {
									S(info).html(parseInt(this.percent) + "%");
								}

							})

					S(bg).mousedown(function(e) {
						this.isDown = true;

					})
					S(bg).mouseup(function(e) {

						if (this.isMoveing) {
							var param = null;
							var r, g, b;
							//alert(this.num);
							if (bg == "brightBg") {
								//param="brightness="+this.num+"";
								$.ajax({
									cache : false,
									type : "POST",
									url : "/index",
									data : {
										brightness : this.num
									},
									async : true
								});
								this.isDown = false;

							} else if (bg == "colorTempBg") {
								//param="colortemp="+this.num+"";
								$.ajax({
									cache : false,
									type : "POST",
									url : "/index",
									data : {
										colortemp : this.num
									},
									async : true
								});

								this.isDown = false;

							}


						}
					})

				}

			}

})





