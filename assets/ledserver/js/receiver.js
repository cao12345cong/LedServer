$(document).ready (function () {
	var row=parseInt($("#receiver_row").val());//hang
	var col=parseInt($("#receiver_col").val());//lie
	
	var receiverNum=1;
	var portNum=1;
	
	var width=64;
	var height=64;
	var receivePostArr=[[[0,0,width,height]]];
	var rePostRowColArr=[[[1,1]]]//port row col;
	initContent();
	var selectedReceiver=0;//start from 0
	var selectedPort=1;
	////
	
	$("#receiver_row_add").click(function(){
		
		var value=parseInt($("#receiver_row").val());
		value=value+1;
		$("#receiver_row").val(value);
		row=value;
		changeContent();
		rePostRowColArr[selectedReceiver][selectedPort-1][0]=row;
		
	})
	$("#receiver_row_minus").click(function(){
		var value=parseInt($("#receiver_row").val());
		if(value>1){
			value=value-1;
		}
		
		$("#receiver_row").val(value);
		row=value;
		changeContent();
		rePostRowColArr[selectedReceiver][selectedPort-1][0]=row;
		
	})
	// input Number
	$("#receiver_row").change(function(){
		var v=$(this).val();
		if(v>255){
			row=255;
		}else if(v<1){
			row=1;
		}else{
			row=v;	
		}
		
		changeContent();
		rePostRowColArr[selectedReceiver][selectedPort-1][0]=row;
	})
	
	
	/////
	$("#receiver_col_add").click(function(){
		
		var value=parseInt($("#receiver_col").val());
		value=value+1;
		$("#receiver_col").val(value);
		col=value;
		changeContent();
		rePostRowColArr[selectedReceiver][selectedPort-1][1]=col;
	})
	$("#receiver_col_minus").click(function(){
		var value=parseInt($("#receiver_col").val());
		if(value>1){
			value=value-1;
		}
		
		$("#receiver_col").val(value);
		col=value;
		changeContent();
		rePostRowColArr[selectedReceiver][selectedPort-1][1]=col;
		
	})
	// Input Number
	$("#receiver_col").change(function(){
		var v=$(this).val();
		if(v>255){
			col=255;
		}else if(v<1){
			col=1;
		}else{
			col=v;	
		}
		
		changeContent();
		rePostRowColArr[selectedReceiver][selectedPort-1][1]=col;
		
	})
	// init Main Content
	function initContent(){
		$("#game_box table").empty();
		
		var tr;
		var td;
		for(var i=0;i<1;i++){ //lie
			
			tr=$("<tr></tr>");
			$("#game_box table").append(tr);
			for(var j=0;j<1;j++){ //hang
				td=$("<td></td>");
				td.attr("id","p"+i+"-"+j);
				tr.append(td);
			}	
		}	
		
	}
	function updateContent(){
		$("#game_box table").empty();
		//alert(selectedPort);
		var row=rePostRowColArr[selectedReceiver][selectedPort-1][0];
		var col=rePostRowColArr[selectedReceiver][selectedPort-1][1];
		//alert(row+",,,,,"+col);
		for(var i=0;i<row;i++){ //lie
			
			tr=$("<tr></tr>");
			$("#game_box table").append(tr);
			for(var j=0;j<col;j++){ //hang
				td=$("<td></td>");
				td.attr("id","p"+i+"-"+j);
				tr.append(td);
			}	
		}
		
		$("#receiver_row").val(row);
		$("#receiver_col").val(col);
		
		
	}
	/////change Main Content
	function changeContent(){
		$("#game_box table").empty();
		
		var tr;
		var td;
		for(var i=0;i<row;i++){ //lie
			
			tr=$("<tr></tr>");
			$("#game_box table").append(tr);
			for(var j=0;j<col;j++){ //hang
				td=$("<td></td>");
				td.attr("id","p"+i+"-"+j);
				tr.append(td);
			}	
		}
		
	}
	
	$("#mode_1").click(function(){
								
		drawlines(1);
		
	})
	$("#mode_2").click(function(){
		drawlines(2);						
	})
	$("#mode_3").click(function(){
		drawlines(3);						
	})
	$("#mode_4").click(function(){
		drawlines(4);						
	})
	$("#mode_5").click(function(){
		drawlines(5);						
	})
	$("#mode_6").click(function(){
		drawlines(6);						
	})
	$("#mode_7").click(function(){
		drawlines(7);						
	})
	$("#mode_8").click(function(){
		drawlines(8);						
	})
	
	function drawlines(mode){
		//alert(mode);
		if(row==1&&col==1){
			return;
		}
		if(mode==1){
			drawModeOne();
		}else if(mode==2){
			drawModeTwo();	
		}else if(mode==3){
			drawModeThree();
		}else if(mode==4){
			drawModeFour();	
		}else if(mode==5){
			drawModeFive();	
		}else if(mode==6){
			drawModeSix();	
		}else if(mode==7){
			drawModeSeven();
		}else if(mode==8){
			drawModeEight();	
		}

	
	}
	
	function drawModeOne(){
		var num=0;
		
		for(var i=0;i<row;i++){
			
			if(i%2==1){
				for(var j=col-1;j>=0;j--){
					var obj=getobj(i,j);
					obj.html(++num);
					obj.css("background","#0099FF url(/mnt/sdcard/ledserver/img/left.gif) center center no-repeat");
				}
			
			}else{
				for(var j=0;j<col;j++){
					var obj=getobj(i,j);
					obj.html(++num);
					obj.css("background","#0099FF url(/mnt/sdcard/ledserver/img/right.gif) center center no-repeat");
				}
			}
			
		}
	}
	function drawModeTwo(){
		var num=0;
		for(var i=0;i<row;i++){
			
			if(i%2==1){
				for(var j=0;j<col;j++){
					var obj=getobj(i,j);
					obj.html(++num);
					obj.css("background","#0099FF url(/mnt/sdcard/ledserver/img/right.gif) center center no-repeat");
				}
			
			}else{
				for(var j=col-1;j>=0;j--){
					var obj=getobj(i,j);
					obj.html(++num);
					obj.css("background","#0099FF url(/mnt/sdcard/ledserver/img/left.gif) center center no-repeat");
				}
			}
			
		}
	}
	function drawModeThree(){
		var num=0;
		for(var i=row-1;i>=0;i--){
			if((row-1-i)%2==1){
				for(var j=col;j>=0;j--){
					var obj=getobj(i,j);
					obj.html(++num);
					obj.css("background","#0099FF url(/mnt/sdcard/ledserver/img/left.gif) center center no-repeat");
				}
			
			}else{
				for(var j=0;j<col;j++){
					var obj=getobj(i,j);
					obj.html(++num);
					obj.css("background","#0099FF url(/mnt/sdcard/ledserver/img/right.gif) center center no-repeat");
				}
			}	
		}
	}
	function drawModeFour(){
		var num=0;
		for(var i=row-1;i>=0;i--){
			if((row-1-i)%2==1){
				for(var j=0;j<col;j++){
					var obj=getobj(i,j);
					obj.html(++num);
					obj.css("background","#0099FF url(/mnt/sdcard/ledserver/img/right.gif) center center no-repeat");
				}
			
			}else{
				for(var j=col-1;j>=0;j--){
					var obj=getobj(i,j);
					obj.html(++num);
					obj.css("background","#0099FF url(/mnt/sdcard/ledserver/img/left.gif) center center no-repeat");
				}
			}	
		}
	}
	function drawModeFive(){
		var num=0;
		
		for(var i=0;i<col;i++){
			
			if(i%2==1){
				for(var j=row-1;j>=0;j--){
					var obj=getobj(j,i);
					obj.html(++num);
					obj.css("background","#0099FF url(/mnt/sdcard/ledserver/img/up.gif) center center no-repeat");
				}
			
			}else{
				for(var j=0;j<row;j++){
					var obj=getobj(j,i);
					obj.html(++num);
					obj.css("background","#0099FF url(/mnt/sdcard/ledserver/img/down.gif) center center no-repeat");
				}
			}
			
		}
	}
	function drawModeSix(){
		var num=0;
		
		for(var i=0;i<col;i++){
			
			if(i%2==1){
				for(var j=0;j<row;j++){
					var obj=getobj(j,i);
					obj.html(++num);
					obj.css("background","#0099FF url(/mnt/sdcard/ledserver/img/up.gif) center center no-repeat");
				}
			
			}else{
				for(var j=row-1;j>=0;j--){
					var obj=getobj(j,i);
					obj.html(++num);
					obj.css("background","#0099FF url(/mnt/sdcard/ledserver/img/down.gif) center center no-repeat");
				}
			}
			
		}
	}
	function drawModeSeven(){
		var num=0;
		for(var i=col-1;i>=0;i--){
			
			if((col-1-i)%2==1){
				for(var j=row-1;j>=0;j--){
					var obj=getobj(j,i);
					obj.html(++num);
					obj.css("background","#0099FF url(/mnt/sdcard/ledserver/img/up.gif) center center no-repeat");
				}
			
			}else{
				for(var j=0;j<row;j++){
					var obj=getobj(j,i);
					obj.html(++num);
					obj.css("background","#0099FF url(/mnt/sdcard/ledserver/img/down.gif) center center no-repeat");
				}
			}
			
		}
	}
	function drawModeEight(){
		var num=0;
		for(var i=col-1;i>=0;i--){
			
			if((col-1-i)%2==1){
				for(var j=0;j<row;j++){
					var obj=getobj(j,i);
					obj.html(++num);
					obj.css("background","#0099FF url(/mnt/sdcard/ledserver/img/up.gif) center center no-repeat");
				}
			
			}else{
				for(var j=row-1;j>=0;j--){
					var obj=getobj(j,i);
					obj.html(++num);
					obj.css("background","#0099FF url(/mnt/sdcard/ledserver/img/down.gif) center center no-repeat");
				}
			}
			
		}
	}
	function getobj(i,j){
		
		return $(["#","p",i,"-",j].join(""));
	}
	
	
	////////////////////////////////////
	$("#receiver_add").click(function(){
			//$("#receicer_show ul").append("<li>	</li>");
		if(receiverNum<15){
			
			$("#receicer_show ul").append("<li>	<span>"+(++receiverNum)+"</span><div><input type='radio' name='receiver' value='"+receiverNum+"'/></div></li>");
		}
		
		receivePostArr[receiverNum-1]=[[0,0,width,height]];
		rePostRowColArr[receiverNum-1]=[[1,1]];
	})
	
	$("#receiver_minus").click(function(){
		if(receiverNum>1){
			receiverNum--;
			$("#receicer_show ul li:last-child").remove();
			
			var len=receivePostArr.length;
			
			receivePostArr.pop();
			rePostRowColArr.pop();
			
		}
	
	})
	
	initPortTable();
	
	function initPortTable(){
		$("#port_talbe2").empty();
		var str="<tr><td>A</td><td>"+
			receivePostArr[0][0][0]+"</td><td>"+
			receivePostArr[0][0][1]+"</td><td>"+
			receivePostArr[0][0][2]+"</td><td>"+
			receivePostArr[0][0][3]+"</td></tr>"
		$("#port_talbe2").append(str)
		$("#port_talbe2 tr:first").css("background","#cccccc");
	}
	//port add button
	$("#port_add").click(function(){
								  
	    var len=receivePostArr[selectedReceiver].length;
		//alert(len);
		var portName;
		
		if(len==1){
			portName="B";
		}else if(len==2){
			portName="C";
		}else if(len==3){
			portName="D";
		}else{
			return;	
		}
		receivePostArr[selectedReceiver][len]=[0,0,width,height];
		
		rePostRowColArr[selectedReceiver][len]=[1,1];
		var str="<tr><td>"+portName+"</td><td>"+
			receivePostArr[selectedReceiver][0][0]+"</td><td>"+
			receivePostArr[selectedReceiver][0][1]+"</td><td>"+
			receivePostArr[selectedReceiver][0][2]+"</td><td>"+
			receivePostArr[selectedReceiver][0][3]+"</td></tr>"
		$("#port_talbe2").append(str);
		
	})
	//port minus
	$("#port_minus").click(function(){
		var len=receivePostArr[selectedReceiver].length;
		if(len>1){
			receivePostArr[selectedReceiver].pop();
			rePostRowColArr[selectedReceiver].pop();
			$("#port_talbe2 tr:last").remove();
		
		}
				
	})
	
	$("input[type='radio']").live("click",function(){
		var v=$("input[type='radio']:checked").val();
		//alert(v);
		selectedReceiver=v-1;
		$("#port_talbe2").empty();
		var len=receivePostArr[v-1].length;
		if(len==0){
			
		}
		
	    for(var i=0;i<len;i++){
			var tr=$("<tr></tr>");
			if(i==0){
				portName="A";
			}else if(i==1){
				portName="B";
			}else if(i==2){
				portName="C";
			}else if(i==3){
				portName="D";
			}
			tr.append("<td>"+portName+"</td>");
			tr.append("<td>"+receivePostArr[v-1][i][0]+"</td>");
			tr.append("<td>"+receivePostArr[v-1][i][1]+"</td>");
			tr.append("<td>"+receivePostArr[v-1][i][2]+"</td>");
			tr.append("<td>"+receivePostArr[v-1][i][3]+"</td>");
			$("#port_talbe2").append(tr);
		}
		$("#port_talbe2 tr:first").css("background","#cccccc");
		updateContent();
	})
	$("#port_talbe2 tr").live("click",function(){
		$("#port_talbe2 tr:selectedPort").css("background","");
		selectedPort=$("#port_talbe2 tr").index($(this))+1;
		$(this).css("background","#cccccc");
		
		updateContent();
	})
	
})