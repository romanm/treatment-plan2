/**
* OWS JavaScript Library
*/
var mmChilds	= Object();
var toolbarId   = ''; 
function init()
{
	var menu_menu=dijit.byId("menu_menu");
	if(menu_menu!=null)
		mmChilds =menu_menu.getChildren();
		// 2012.10.15 rem for test class selected
	//addCopyPasteFunctionality();
	getCookiePageYOffset();	
}

function dateControl(d) {
	console.log("----dateControl(d)");
	console.log("----dateControl(d)"+d);
	console.log("----dateControl(d)"+dijit.byId("bdate"));
	
//	console.log("----dateControl(d)"+d.value);
//	console.log("----dateControl(d)"+dijit.byId("bdate"));
//	console.log("----dateControl(d)"+d.value);
//	console.log("----dateControl(d)"+d.value.substring(0,2));
//	console.log("----dateControl(d)"+d.value.substring(3,5));
////	var d = new Date(d.value);
//	var dd = new Date();
//	dd.setHours(0, 0, 0, 0);
//	console.log("----dateControl(d)"+dd);
//	dd.setDate(d.value.substring(0,2));
//	console.log("----dateControl(d)"+dd);
//	var today = new Date();
//	console.log("----dateControl(d)"+today);
//	today.setHours(0, 0, 0, 0);
//	return Math.abs(dojo.date.difference(dd, today, "week")) > 0;
}

function dijitFocus(id)
{
	var idElement = dojo.byId(id);
	if(null!=idElement )
		dijit.focus(idElement);
	return idElement;
}

function focusElement(element)
{
	if(null!=element )
		dijit.focus(element);
	return element;
}

//remove the zero in the domNode
function removeZero(domNodeName)
{
	var n = dojo.byId(domNodeName);
	if(n==null)
		return;
	if(n.value == 0)
		n.value='';
	return n;
}

//used mainly for inputfields (in saveNewPatient, bsaDialog.tagx)
//just trick around if a widget gets a focus 
//and then looses that it validates the input 
//and since the input is null and not a digit, the box gets red
function removeZeroAndRefocus(widget1, widget2)
{
	//dont change the order!!!
	var w  = removeZero(widget1);
	dijit.focus(w);
	var w2 = removeZero(widget2);
	dijit.focus(w2);
	dijit.focus(w);
}

function createInputFieldWidget(idElement, regularExpr, errorMsg)
{
	Spring.addDecoration(new Spring.ElementDecoration(
	{
		elementId : idElement,
		widgetType : "dijit.form.ValidationTextBox",
		widgetAttrs : 
		{ 
			required : true, 
			invalidMessage : errorMsg, 
			regExp : regularExpr
		}
	}));
}

function createSelectWidget(idElement)
{
	Spring.addDecoration(new Spring.ElementDecoration(
			{
				elementId : idElement,
				widgetType : "dijit.form.Select",
				widgetAttrs : 
				{ 
					required : true,
					maxHeight:260
				}
			}));
}


function checkLength2(e, input, maxLength, minLength) {
	var l = input.value.length;
	if (l > 0 && isBackspace(e)) l--;
	if ( ! isControl(e)) l++;
	//alert(l);
	if (l >= maxLength || l < minLength) {
		input.className = "warning";
	} else {
		input.className = "focus";
	}
	return true;
}
function isControl(e) {
	var key = getKey(e);
	return ! key || key == 8 || key == 9 || key == 13 || key == 27;
}
function getKey(e) {
	if (window && window.event) return window.event.keyCode;
	if (e) return e.which;
	return null;
}
function isBackspace(e) {
	var key = getKey(e);
	return key == 8;
}
function nextDateId(){
	alert("Hello3");
}
///////////////////////////////////////////////
// Standard Class Extension 
///////////////////////////////////////////////
String.prototype.trim = function() 
{
	return this.replace(/^\s+|\s+$/g,"");
};
String.prototype.ltrim = function() {
	return this.replace(/^\s+/,"");
};
String.prototype.rtrim = function() {
	return this.replace(/\s+$/,"");
};

///////////////////////////////////////////////
// Global Functions 
///////////////////////////////////////////////
function trim(stringToTrim) {
	return stringToTrim.replace(/^\s+|\s+$/g,"");
}
function ltrim(stringToTrim) {
	return stringToTrim.replace(/^\s+/,"");
}
function rtrim(stringToTrim) {
	return stringToTrim.replace(/\s+$/,"");
}

///////////////////////////////////////////////
// OWS - Revise Functions 
///////////////////////////////////////////////
function laborReviseOnClick(labor, unit)
{
	console.log("----laborReviseOnClick()");
	
	var a = [];	
	str = dojo.byId(labor).value;
	a = str.split(" | ");
	//console.log("laborReviseOnClick():  - labor: "+ a[0]+"  --- unit:" + a[1]);
	dojo.byId(labor).value = a[0];	
	dojo.byId(unit).value = a[1];
	//console.log("laborReviseOnClick():saved back  - labor: "+ dojo.byId(labor).value+"  --- unit:" + dojo.byId(unit).value);
	
	if(a.length > 0)
		str = a[1];
	
	dojo.byId(labor).value = trim(dojo.byId(labor).value);
	dojo.byId(unit).value  = trim(dojo.byId(unit).value);
	
	console.log("----labor:" + dojo.byId(labor).value + "---unit:" + dojo.byId(unit).value);
	//alert("laborReviseOnClick()");
}


///////////////////////////////////////////////
//OWS - Ajax Functions 
///////////////////////////////////////////////
//put this in a tag and use the tag in menu.jsp, give the tree the name of this tag so it will be set automaticly
function explorerAjaxRequest(urlBase, rootFolderName, idFolder, searchStr, containerToKill)
{
	dojo.xhrGet
	(
		{
			url : ""+urlBase + "?folder="+rootFolderName+"&id=" + idFolder+ "&search="+searchStr,
			load : function(response, ioArgs)
			{
				dojo.byId(""+urlBase+"Container").innerHTML = response;
				console.log("remove "+ containerToKill);
				if(containerToKill != null)
					dojo.byId(""+containerToKill).innerHTML = "";
				
				return response;
			},
			error : function(response, ioArgs)
			{
				dojo.byId(""+urlBase+"Container").innerHTML = "An error occurred, with response: "
						+ response;
				return response;
			},
			handleAs : "text"
		}
	);
}//end getList	

function explorerAjaxRequestDialog(urlBase, eventId,  rootFolderName,  idFolder)
{
	console.log("---");
	dojo.xhrGet({
			url : ""+urlBase + "?_eventId="+eventId+"&folder="+rootFolderName+"&id=" + idFolder,
			load : function(response, ioArgs)
			{
				dojo.byId("explorerMenuDialog").innerHTML = response;;
				
				return response;
			},
			error : function(response, ioArgs)
			{
				dojo.byId(""+urlBase+"Container").innerHTML = "An error occurred, with response: "
						+ response;
				return response;
			},
			handleAs : "text"
		}
	);
}//end getList	


// invisible aHref link
function clickInvisibleAHrefLink(url)
{
	var linkId='invisibleAHrefLink';
	var a = dojo.byId(linkId);
	if( a != null)
	{
		dojo.attr(a, 'href', url);
	}else{
		dojo.attr(a, 'href', url);
	}
	return a;
}
// invisible ajax link
function clickInvisibleSpringAjaxLink(url, fragmentList)
{
	//reuse the invisible Link or make a new one
	var linkId='invisibleSpringAjaxLink';
	var a = dojo.byId(linkId);
	console.log('log a=' + a);
	if( a != null)
	{
		dojo.attr(a, 'href', url);
	}
	else
	{
		a = dojo.doc.createElement('a');
		dojo.attr(a, 'href', url);
		dojo.attr(a, 'id', linkId);
		dojo.body().appendChild(a);

		Spring.addDecoration(new Spring.AjaxEventDecoration({
			elementId:linkId,
			event:"onclick",
			params: {fragments:fragmentList}
		}));

		a = dojo.byId(linkId);
		console.log('log a=' + a);
	}
	

	//simulate click
	console.log( (typeof a=='object'));
	if (typeof a == 'object') 
	{
	        /*if(typeof e.click != 'undefined') {
	            e.click();
	            alert('click');
	            return false;
	        }
	        else */
	        if(document.createEvent) {
	            var evObj = document.createEvent('MouseEvents');
	            evObj.initEvent('click',true,true);
	            a.dispatchEvent(evObj);
//	            alert('createEvent');
	            return false;
	        }
	        else if(document.createEventObject) {
	            a.fireEvent('onclick');
//	            alert('createEventObject');
	            return false;
	        }
	        else {
	            a.click();
//	            alert('click');
	            return false;
	        }
	    }
	
}

///////////////////
//  gui functions
///////////////////
function clickOK(){
	var wlsp=window.location.search.split("execution=");
	self.location.href='?execution='+wlsp[1]+'&_eventId=ok';
}
function cancel(){
	var wlsp=window.location.search.split("execution=");
	self.location.href='?execution='+wlsp[1]+'&_eventId=cancel';
}

function titlePane(tpId,isOpened){
	Spring.addDecoration(new Spring.ElementDecoration({
	elementId:tpId, widgetType:"dijit.TitlePane" ,widgetAttrs: {
		title:this.title,open:isOpened
	}
	}));
}
//TODO: this func is used only in mtl folder and this one is depricated, -> remove it
function decorateAjaxOnclick (id,fragment) {
	Spring.addDecoration(new Spring.AjaxEventDecoration({
		elementId: id,
		event: "onclick",
		params: {fragments: fragment}
	}));
}

function addCopyPasteFunctionality_old(){
	dojo.query("td.select, th.select, span.select, div.select, li.select")
	.onclick(function(e){
		var editE=e.target;
		if(editE.getAttribute("id")==null)return;
		var idec=dojo.byId('copyE');
		var idcE=dojo.byId('idc');
		var idArray=editE.getAttribute("id").split("_");
		if(idArray.length==1) return;
		idcE.value=idArray[1];
		if(null!=editE.getAttribute("idtimes")){
			idArray=editE.getAttribute("idtimes").split("_");
			var idtimes=idArray[1];
			console.log("idtimes="+idtimes);
		}

		var inputL=dojo.query("#"+editE.getAttribute("id")+" input[id!='reviseNode']");
		if(inputL.length>0){
			var str="";
			inputL.forEach(function(node, index, arr){
				console.log("---"+node.value);
				str+=node.value;
			});
			console.log("-2--"+str);
			idec.innerHTML=str;
		}else{
			idec.innerHTML=editE.innerHTML;
		}
			idec.innerHTML=editE.innerHTML;
		dojo.connect(
			dojo.anim(editE, {backgroundColor: "#9CAC7C"},200),
			"onEnd", function() {dojo.anim(editE, {backgroundColor: "white;"}, 200);}
		);
	});
}

function selectElement_old(editE){
	if("IMG"==editE.tagName){
		editE=editE.parentNode;
	}
	console.log(editE);
	console.log(editE.id);
	console.log("idtimes---1--");
	if(dojo.hasClass(editE,"selected")){
		dojo.removeClass(editE, "selected");
		return;
	}
	if(!dojo.hasClass(editE,"select")){//seek outer *.select
		editE=editE.parentNode;
		console.log("-02-1-"+editE.id);
	}
	dojo.query(".selected").removeClass("selected");
	dojo.addClass(editE, "selected");
	if(editE.getAttribute("id")==null)
		return;
	setIdc(editE);
}
function addCopyPasteFunctionality(){
	console.log("addCopyPasteFunctionality1");
	dojo.query("td.select, th.select, caption.select, span.select, div.select, li.select").onclick(function(e){
		console.log("addCopyPasteFunctionality2");
		var editE=e.target;
		console.log(editE);
		// 2012.10.15 rem for test
		//selectElement(editE);
	});
}
function getDojoMonthDay(){//json object
	var bdDayListe = {items:[{"name":1}],label:"name",identifier:"name"};
	for ( var i = 2; i <= 31; i++) {
		bdDayListe.items.push( { "name":i } );
	}
	return bdDayListe;
}
function correcturSelectElement(editE){
	console.log("correcturSelectElement 3 "+editE);
	for ( var i = 0; i < 4; i++) {
		console.log("correcturSelectElement 3 1 ");
		if(dojo.hasClass(editE,"select")){
			break;
		}
		editE=editE.parentNode;
	}
	console.log("correcturSelectElement 4 "+editE);
	return editE;
}
function selectElement(editE){
	console.log("selectElement");
	editE= correcturSelectElement(editE);
	console.log(editE);
	if(dojo.hasClass(editE,"selected")){
	console.log("selectElement removeClass");
		dojo.removeClass(editE, "selected");
		return;
	}
	dojo.query(".selected").removeClass("selected");
	dojo.addClass(editE, "selected");
	console.log(editE);
	console.log(editE.getAttribute("id"));
	console.log(editE.getAttribute("id")==null);
	if(editE.getAttribute("id")==null)
		return;
	console.log(editE.getAttribute("id"));
	console.log("selectElement setIdc 1");
	setIdc(editE);
}
function setIdc(clickE){
	dojo.require("dojo.number");
	console.log("setIdc");
	clickE= correcturSelectElement(clickE);
	console.log(clickE);
	//var idcE=dojo.byId('idc');
	var idArray=clickE.getAttribute("id").split("_");
//	console.log(idArray);
//	console.log(idArray[0]);
//	console.log(idArray.length);
	if(idArray.length==1){
		console.log(idArray);
		return;
	}
	dojo.byId('prefixc').value=idArray[0];
	if(dojo.number.format(idArray[1])!=null)
		dojo.byId('idc').value=idArray[1];
}
function hide(nodeId){
	dojo.byId(nodeId).addClass("hidden");
}
function show(nodeId){
	dojo.byId(nodeId).addClass("visible");
}


////////////////////////////////////////////////
//
//  dojo.back functions
//
////////////////////////////////////////////////

/*
ApplicationState = function(stateData, outputDivId, backForwardOutputDivId, bookmarkValue){
	this.stateData = stateData;
	this.outputDivId = outputDivId;
	this.backForwardOutputDivId = backForwardOutputDivId;
	this.changeUrl = bookmarkValue;
}

dojo.extend(ApplicationState, {
      back: function(){
              this.showBackForwardMessage("BACK for State Data: " + this.stateData);
              this.showStateData();
      },
      forward: function(){
              this.showBackForwardMessage("FORWARD for State Data: " + this.stateData);
              this.showStateData();
      },
      showStateData: function(){
              dojo.byId(this.outputDivId).innerHTML += this.stateData + '<br />';
      },
      showBackForwardMessage: function(message){
              dojo.byId(this.backForwardOutputDivId).innerHTML += message + '<br />';
      }
});
 * */

var data = 
{
      link0: "This is the initial state (page first loaded)",
      link1: "menuDialog",
      link2: "menuFolderTree" ,
      link3: "fragContent"
};
	  	
function goNav(id){
     var appState = new ApplicationState(data[id], "output", "dataOutput", id);
     appState.showStateData();
     dojo.back.addToHistory(appState);
}
/*
dojo.addOnLoad(function(){
    var appState = new ApplicationState(data["link0"], "output", "dataOutput");
    appState.showStateData();
    dojo.back.setInitialState(appState);
});
 * */


////////////////////////////////////////////////
//
//  dojo.cookie functions
//
////////////////////////////////////////////////
logCookie = function() {
	console.log("pageYOffset=" + dojo.cookie("pageYOffset"));
};

//fix for IE7
function getPageYOffset()
{
	var topPosFix = typeof window.pageYOffset != 'undefined' ?
	window.pageYOffset:document.documentElement &&
	document.documentElement.scrollTop ?
	document.documentElement.scrollTop: document.body.scrollTop?
	document.body.scrollTop:0;
//	alert('window.pageYOffset='+window.pageYOffset+' document.documentElement.scrollTop='+document.documentElement.scrollTop
//	+' document.body.scrollTop='+document.body.scrollTop)
	return topPosFix;
}

function setCookiePageYOffset () 
{
	dojo.cookie("pageYOffset", 	getPageYOffset());
//	alert("loaded YOffset");
//	dojo.cookie("pageYOffset", pageYOffset);
	//dojo.cookie("pageYOffset", pageYOffset, { expires: 1});
};


function getCookiePageYOffset() {window.scrollTo(0, dojo.cookie("pageYOffset"));}

////////////////////////////////////////////////
//
//  dojo Menu function
//
////////////////////////////////////////////////
function clickLink(link){
	if(link.hasAttribute("onclick")){
		dojo.eval(link.getAttribute("onclick"));
	}else{
		self.location.href=link.href;
	}
}
function makeActionLinkClick(action,idt){
	if(window.location.search.indexOf("execution=")>0){
		if(null==action || ""==action){
		}else if("modDay"==action){
			dojo.byId('action').value='modDay';
			//dojo.byId('modDay').value=modDay;
			//dojo.byId('tsNr').value=ts.nr;
		}else{
			dojo.byId('action').value=action;
		}
		dojo.byId('idt').value=idt;
		dojo.byId('editStep').click();
	}else{
		if("modDay"==action){
			//self.location.href=url1+"?id="+docId+"&a=modDay&modDay="+modDay+"&tsNr="+ts.nr;
		}else{
			self.location.href=url1+"?id="+docId+"&idt="+idt+"&a="+action;
		}
	}
}
function findClickLink(cssSelector){
	console.log("---"+cssSelector);
	console.log("---"+dojo.query(cssSelector));
	dojo.query(cssSelector).forEach(function(link){
		console.log("--2-"+cssSelector);
		clickLink(link);
		return;
	});
}
function edit(){
	//findClickLink(".selected a");
	dojo.query(".selected a").forEach(function(link){
		clickLink(link);
		return;
	});
	dojo.query("a.selected").forEach(function(link){
		clickLink(link);
		return;
	});
	
}
function getUrl1(){return url1;}
function armName(){
	var idcE = getIdcE();
	if(idcE.value.length!=0){
		idcAction("armName");
	}else{
		alert(cpCopyHelp);
	}
}
function getIdcE(){
	var idcE = dojo.byId('idc');
	if(idcE.value.length==0){
		dojo.query(".selected").forEach(function(node){
			selectElement(node);
		});
	}
	return idcE;
}
function conceptName(protocolId){
	urlStr='conceptflow?id='+protocolId+'&a=editConceptNameAndO';
	self.location.href=urlStr;
}
function getIdcHtmlE(){
	var idcE = getIdcE();
	var prefixcE = dojo.byId('prefixc');
	var id=prefixcE.value+"_"+idcE.value;
	console.log("id="+id);
	return dojo.byId(id);
}
function tab(){
	var idcHtmlE=getIdcHtmlE();
	if("a"!=idcHtmlE.tagName){
		idcHtmlE = idcHtmlE.getElementsByTagName("a")[0];
	}
	window.open(idcHtmlE,"_newtab");
}
function cut(){
	console.log("--cut-");
	var idcE = getIdcE();
	if(idcE.value.length!=0){
		idcAction("cut");
	}else{
		alert(cpCopyHelp);
	}

}
function copy(){
//	console.log("--copy-");
	var idcE = getIdcE();
	if(idcE.value.length!=0){
		idcAction("copy");
	}else{
		alert(cpCopyHelp);
	}
}
	
function copy_old(){
	console.log("--copy-");
	var idcE = dojo.byId('idc');
	console.log("--copy-"+idcE);
	console.log("--copy-"+idcE.tagName);
	if(idcE.value.length==0){
		
		alert(cpCopyHelp);
	}else{
		idcAction("copy");
	}
}
function paste()		{
	console.log("-paste--");
	idcAction("paste");
}
function newDDNotice()	{idtAction("newNotice");}
function newRule()		{idtAction("newRule");}
function cmDayDelay()	{idtAction("cmDayDelay");}

function newAdditionalDrug(){idtAction("newAdditionalDrug");}
function linkOtherDrug(idt,tsNr){
	var wlsp=window.location.search.split("execution=");
	if(wlsp.length==1){
		self.location.href='smod?id='+docId+'&idt='+idt+'&tsNr='+tsNr;
	}else{
		dojo.byId('idt').value=idt;
		dojo.byId('tsNr').value=tsNr;
		dojo.byId('submitOtherDrug').click();
	}
}
function idtAction(action){
	var idcE = dojo.byId('idc');
	console.log(idcE);
	console.log(idcE.value);
	//selfHref(action,'&idt='+idcE.value);
	var url="#";
	var wlsp=window.location.search.split("execution=");
	if(wlsp.length==1){
		url=getUrl1() + '?id='+docId+'&a='+action+'&idt='+idcE.value;
		//url=getUrl1() + '?id='+docId+'&a='+action+'&part='+part+'&idt='+idcE.value;
	}else{
		//url='?execution=' + wlsp[1] + '&_eventId=' + action + addUrlKey;
		dojo.byId('idt').value=idcE.value;
		dojo.byId('action').value=action;
		dojo.byId('editStep').click();
	}
	self.location.href=url;
}
function idcAction(action){
	console.log("-idcAction-1-"+action);
	var idcE = dojo.byId('idc');
	if(null==idcE)
		alert("null==idcE");
	var prefixcE = dojo.byId('prefixc');
	var idcUrlKey = '&idc='+idcE.value+'&prefixc='+prefixcE.value;
	console.log(idcUrlKey);
	selfHref(action,idcUrlKey);
}
function selfHref(action,addUrlKey){
	var url="#";
	var wlsp=window.location.search.split("execution=");
	if(wlsp.length==1){
		url=getUrl1() + '?id='+docId+'&a='+action+'&part='+part+addUrlKey;
	}else{
		url=getUrl1() + '?id='+docId+'&a='+action+'&part='+part+addUrlKey;
		//url='?execution=' + wlsp[1] + '&_eventId=' + action + addUrlKey;
	}
	console.log(url);
	self.location.href=url;
}

///////////////////////////////////////////////////////////////////////////
//
//	Handle clicks on Schema (Drug) Tree Node (delete move drugs)
//
///////////////////////////////////////////////////////////////////////////
function del(){
	handleSchemaNode('deleteNode');
}
function nodeUp(){
	handleSchemaNode('upNode');
}

function nodeDown(){
	handleSchemaNode('downNode');
}

function handleSchemaNode(action)
{
	//var idcE = dojo.byId('idc');
	var idcE = getIdcE();
	if(idcE.value.length==0){
		alert(cpCopyNull);
	}else{
		handleNode(idcE.value, action);
	}
}


///////////////////////////////////////////////////////////////////////////
//
//	Handle clicks on Explorer Tree Node
//
///////////////////////////////////////////////////////////////////////////
function deleteTreeNode()
{
	handleExplorerTreeNode('deleteTreeNode');
}
function newProtocolTCM()
{
	handleExplorerTreeNode('newProtocol');
}

function newTreeNode()
{
	handleExplorerTreeNode('newTreeNode');
}
function renameTreeNode()
{
	handleExplorerTreeNode('renameTreeNode');
}
function moveTreeNodeUp()
{
	handleExplorerTreeNode('upNode');
}

function moveTreeNodeDown()
{
	handleExplorerTreeNode('downNode');
}

function handleExplorerTreeNode(action)
{
	var idE = dojo.byId("treeMenu").getAttribute("fid");
	console.log(idE);
	if(idE.length==0){
		console.log("handleExplorerTreeNode Exception");
	}else{
		handleNode(idE, action);
	}
}

function handleNode(id, action){
	var wlsp=window.location.search.split("execution=");
	console.log(getUrl1()+'?id='+docId+'&a='+action+'&idc='+id);
	
	if(wlsp.length==1){
		self.location.href=getUrl1()+'?id='+docId+'&a='+action+'&idc='+id;
	}else{
		dojo.byId('action').value=action;
		dojo.byId('idc').value=id;
		dojo.byId('editStep').click();
	}
}

////////////////////////////////////////////////
//
// dojo Menu system
//
////////////////////////////////////////////////
function moveMenuItems(){
	console.log("moveMenuItems 1 ");
	dojo.query(".select").connect("oncontextmenu", function(e) {
		console.log("moveMenuItems 2 "+e.target);
		var editE=correcturSelectElement(e.target);
		console.log("editE="+editE);
		dojo.query(".selected").removeClass("selected");
		dojo.addClass(editE, "selected");
//		dojo.addClass(e.target, "selected");
		setIdc(e.target);
		var isDrugDose		= hasClass(editE, "drug")||hasClass(editE, "dose");
		var isNewDDNotice	= hasClass(editE, "dose")||hasClass(editE, "day");
		var isMiUpdate		= isDrugDose||isNewDDNotice
		||hasClass(editE, "taskNotice")||hasClass(editE, "schema");
		moveMenuItem(isMiUpdate,				"mi_cmus1");
		moveMenuItem(isDrugDose,				"cmNewAdditionalDrug");
		moveMenuItem(isDrugDose||hasClass(editE, "day"),	"cmNewRule");
		moveMenuItem(isNewDDNotice||isDrugDose,	"cmNewDDNotice");
		moveMenuItem(hasClass(editE, "schema"),	"mi_editCycle");
		moveMenuItem(hasClass(editE, "taskNotice"),	"addNotice");
		moveMenuItem(hasClass(editE, "schema"),		"mi_newDrug");
		moveMenuItem(hasClass(editE, "schema"),		"mi_newSupportDrug");
		moveMenuItem(hasClass(editE, "schema"),		"mi_newOnDemandDrug");
		moveMenuItem(hasClass(editE, "arm"),		"cmArmName");
		console.log("editE schema "+hasClass(editE, "schema"));
		console.log("editE cmDayDelay "+hasClass(editE, "cmDayDelay"));
		moveMenuItem(hasClass(editE, "cmDayDelay"),	"cmDayDelay");
		moveMenuItem(isDrugDose,				"innerTask");
	});
}
/**
 * Move menu item 
 * @param isMyClass
 * @param childId
 */
function moveMenuItem(isMyClass,childId){
	var ctxMenuName="ctxMenuUpdate";
	if(isMyClass){
		ctxMenuName="contextMenu";
	}
	var ctxMenu=dijit.byId(ctxMenuName);
	var childE=dijit.byId(childId);
	if(childE!=null){
		ctxMenu.addChild(childE);
	//}else{
	//	console.log(childId);
	}
}
function hasClass(editE,className){return dojo.hasClass(editE,className);}

function toolbar(id){
	toolbarId = id;
	Spring.addDecoration(new Spring.ElementDecoration(
		{elementId:id, widgetType:"dijit.Toolbar"}
	));
}
function toolbarSeparator(id){
	Spring.addDecoration(new Spring.ElementDecoration(
		{elementId:id, widgetType:"dijit.ToolbarSeparator", widgetModule:"dijit.Toolbar"}
	));
}
function dropDownButton(id,label){
	Spring.addDecoration(new Spring.ElementDecoration({
		elementId:id, widgetType:"dijit.form.DropDownButton", widgetModule:"dijit.form.Button",
		widgetAttrs : {label:label, onFocus: function(){
			var menu_menu=dijit.byId("menu_menu");
			for(var i=0;i<mmChilds.length;i++){
				menu_menu.addChild(mmChilds[i]);
			}
		}}
	}));
}
function menu(id){
	Spring.addDecoration(new Spring.ElementDecoration(
		{elementId:id, widgetType:"dijit.Menu"}
	));
}
function menuSeparator(id){
	Spring.addDecoration(new Spring.ElementDecoration({
		elementId:id, widgetType:"dijit.MenuSeparator", widgetModule:"dijit.Menu"
	}));
}
function menuItemInnerTask(id){
	console.log("-- id = "+id);
	Spring.addDecoration(new Spring.ElementDecoration({
		elementId:"mi_"+id, widgetType:"dijit.MenuItem", widgetModule:"dijit.Menu",
		widgetAttrs : {onClick: function(){idtAction(id);}}
	}));
}
function checkedMenuItemLinkClick(id,classes,sameDayTogether){
	Spring.addDecoration(new Spring.ElementDecoration({
		elementId:id, widgetType:"dijit.CheckedMenuItem", widgetModule:"dijit.Menu",
		widgetAttrs : {iconClass:classes,checked:sameDayTogether, onClick: function(){findClickLink("#"+id+"Link a");}}
	}));
}
function menuItemClick(id,classes,idt,asuffix){
	var action=id;
	if(null!=asuffix) action+=asuffix;
	Spring.addDecoration(new Spring.ElementDecoration({
		elementId:"mi_"+id, widgetType:"dijit.MenuItem", widgetModule:"dijit.Menu",
		widgetAttrs : {iconClass:classes, onClick: function(){makeActionLinkClick(action,idt);}}
	}));
}
function menuItemLinkClick(id,classes){
	Spring.addDecoration(new Spring.ElementDecoration({
		elementId:id, widgetType:"dijit.MenuItem", widgetModule:"dijit.Menu",
		widgetAttrs : {iconClass:classes, onClick: function(){findClickLink("#"+id+"Link a");}}
	}));
}
function menuItem(id,classes,click){
	Spring.addDecoration(new Spring.ElementDecoration({
		elementId:id, widgetType:"dijit.MenuItem", widgetModule:"dijit.Menu",
		widgetAttrs : {iconClass:classes, onClick: click}
	}));
}
function popupMenuItem(id,label,classes){
	Spring.addDecoration(new Spring.ElementDecoration({
		elementId:id, widgetType:"dijit.PopupMenuItem", widgetModule:"dijit.Menu",
		widgetAttrs : {label:label, iconClass:classes} 
	}));
}
////////////////////////////////////////////////
//
// edit app
//
////////////////////////////////////////////////
function changeAppapp(editId){
	dojo.byId("appapp0").value=dojo.byId("appApp").value;
	if(dojo.byId("um1").checked)	dojo.byId("appUnit").value="min";
	if(dojo.byId("uh1").checked)	dojo.byId("appUnit").value="h";
	if(dojo.byId("ud1").checked)	dojo.byId("appUnit").value="d";
	dojo.byId("dhm1App_"+editId).innerHTML=dojo.byId("appApp").value;
	dojo.byId("dhm0_"+editId).innerHTML="";
	//dojo.byId("dhm1Tempo_${editId}").innerHTML="";
}
function tempo1(dhm,editId){
	console.log("---- 1 "+dhm);
	dojo.byId("dhm1Unit_"+editId).innerHTML=dhm;
	console.log("---- 2 ");
	changeAppapp();
}
function tempo0(dhm,editId){
	dojo.byId("dhm0_"+editId).innerHTML=dojo.byId(dhm+"l").innerHTML;
	dojo.byId("dhm1Unit_"+editId).innerHTML="";
	dojo.byId("dhm1App_"+editId).innerHTML="";
	//dojo.byId("dhm1Tempo_${editId}").innerHTML="";
	dojo.byId("appapp0").value=0;
	dojo.byId("appUnit").value=dojo.byId(dhm).value;
}

////////////////////////////////////////////////
//
// edit day
//
////////////////////////////////////////////////
function dayAbs()
{
	var days="";
	if(dojo.byId("setday_-1").checked)	days += "-1, ";
	if(dojo.byId("setday_-2").checked)	days += "-2, ";
	if(dojo.byId("setday_-3").checked)	days += "-3, ";
	console.log("duration="+duration);
	for(var d = 1; d <= duration; d++)
	{
		var dayId="setday_"+d;
		if(dojo.byId(dayId)!=null && dojo.byId(dayId).checked)
		{
			days += d+", ";
		}
	}
	if(days.length>0) days = days.substring(0,days.length-2);
	dojo.byId("inputDay").innerHTML=days;
	dojo.byId("editDayAbs").value=days;
	console.log(dojo.byId("editDayAbs"));
	dojo.byId("dayNewtype").value='a';
}
function timesRelativDeep(deepRadio){
	dojo.byId("editDayAbs").value=deepRadio.value;
	console.log(dojo.byId("editDayAbs").value);
}
function timesRelativF(){
	var tl=dojo.byId("timesRelativ");
	dojo.byId("inputDay").innerHTML="timesRelativ "+tl.checked;
	if(tl.checked){
		dojo.byId("dayNewtype").value="timesRelativ";
	}
}
function forRule(){
	var gc=dojo.byId("giveCycleSelect").value;
	console.log("1 c="+gc);
	if(gc!="c")	gc="g";
	console.log("2 c="+gc);
	dojo.byId("giveCycleSelect").value=gc;
	var dw=dojo.byId("dayWeekSelect").value;
	if(dw!="w")	dw="d";
	dojo.byId("dayWeekSelect").value=dw;
	var forRule="forRule_"+gc+"_"+dw;
	console.log("-forRule-"+forRule);
	
	var startDay=dojo.byId("startDaySelect").value;
	var repetition=dojo.byId("repetitionSelect").value;
	var abs=startDay+","+repetition;
	var startDayAs =" als erste Gabe";
	if(gc=="c")
		startDayAs =" in diese Zyklus";
	var repetitionUnit =" Tag";
	if(dw=="w")
		repetitionUnit =" Woche";
	var forRuleText="Tag " +	startDay +startDayAs+
			" dann periodisch jede " +
			"" +repetition+repetitionUnit;
	console.log("-forRuleText-"+forRuleText);
	dojo.byId("dayNewtype").value=forRule;
	dojo.byId("editDayAbs").value=abs;
	dojo.byId("inputDay").innerHTML=forRuleText;
}
function dayPeriod(select){
	console.log("-01-");
	var sinceE= dojo.byId("since");
	var tillE= dojo.byId("till");
	for(var i = 0; i < sinceE.options.length-1; i++)
	{
		if (tillE.options[i].value <= sinceE.selectedIndex+1)
		{
			tillE.options[i].disabled="disabled";
		} else
			tillE.options[i].disabled="";
	}
	if (sinceE.selectedIndex >= tillE.selectedIndex
		&& sinceE.selectedIndex != sinceE.options.length-1)
		tillE.options[sinceE.selectedIndex+1].selected="selected";
	
	days = sinceE.options[sinceE.selectedIndex].text+"-"+
	tillE.options[tillE.selectedIndex].text;
	dojo.byId("inputDay").innerHTML=days;
	dojo.byId("editDayAbs").value=days;
	dojo.byId("dayNewtype").value='p';
}
function dayWeek(){
	var days="";
	wday=new Array('So','Mo','Di','Mi','Do','Fr','Sa');
	for(var d = 0; d <= 6; d++)
	{
		var dayId="weekday_"+d;
		if(dojo.byId(dayId)!=null && dojo.byId(dayId).checked)
		{
			days += wday[d]+", ";
//			days += (d+1)+", ";
		}
	}
	if(days.length>0) days = days.substring(0,days.length-2);
	dojo.byId("inputDay").innerHTML=days;
	dojo.byId("editDayAbs").value=days;
	dojo.byId("dayNewtype").value='w';
}
////////////////////////////////////////////////
//
// edit times
//
////////////////////////////////////////////////

function absTimes(){
	var abs="";
	for(var h = 0; h <= 23; h++) {
		var atId="at_"+h;
		if(dojo.byId(atId)!=null && dojo.byId(atId).checked){
			if(h<10)abs+="0";
			abs+=h+":00, ";
		}
	}
	if(abs.length>0)
		abs=abs.substring(0,abs.length-2);
	console.log(abs);
	dojo.byId("timesAbs").value=abs;
	dojo.byId("inputTimes").innerHTML=abs;
	dojo.byId("timesRef").value=0;
}
function meals(){
	var meals=""
		+(dojo.byId("meals0").checked?"1":"0")+"-"
		+(dojo.byId("meals2").checked?"1":"0")+"-"
		+(dojo.byId("meals4").checked?"1":"0")+"-"
		+(dojo.byId("meals6").checked?"1":"0")
		;
	console.log(meals);
	dojo.byId("inputTimes").innerHTML=meals;
	var meals2=meals.replace("-","=").replace("-","=").replace("-","=").replace("-","=");
	console.log(meals2);
	dojo.byId("timesAbs").value=meals2;
}
function cancelReload(){
	self.location.href=dojo.byId('reload').href;
}
function toBeginEnd(relvalue,apporder){
	dojo.byId("timesApporder").value	= apporder;
	dojo.byId("timesRef").value	= relvalue;
	dojo.byId('editStep').click();
}
function relSMH(){
	var relvalue		= dojo.byId("relvalue");
	var apporder		= dojo.byId("apporder");
	var refTimes		= dojo.byId("refTimes");
	console.log("refTimes="+refTimes.value);
	var tsNr			= dojo.byId("ro"+refTimes.value).getAttribute("tsnr");
	//var visual			= dojo.byId("visual");
	var timesRelunit	= dojo.byId("timesRelunit");
	var tSMH = "";
	dojo.byId("timesRef").value			= refTimes.value;
	console.log("timesRef="+dojo.byId("timesRef").value);
	dojo.byId("timesApporder").value	= apporder.value;
	dojo.byId("timesRelvalue").value	= relvalue.value;
	//dojo.byId("timesVisual").value		= visual.value;
	console.log("M"+dojo.byId("relunitM").checked);
	console.log("H"+dojo.byId("relunitH").checked);
	console.log("S"+dojo.byId("relunitS").checked);
	if(dojo.byId("relunitM").checked) {
		timesRelunit.value="M";
		tSMH =uiTimesM;
	}else if(dojo.byId("relunitH").checked) {
		timesRelunit.value="H";
		tSMH =uiTimesH;
	}else if(dojo.byId("relunitS").checked) {
		timesRelunit.value="S";
		tSMH ="";
		relvalue.value="";
	}
	console.log(timesRelunit);
	var tApporder ="";
	if(apporder.value==2)		tApporder =".&gt;.";
	else if(apporder.value==0)	tApporder ="..&gt;";
	else if(apporder.value==1)	tApporder ="&lt;..";
	else if(apporder.value==3)	tApporder ="&gt;..";
	var relSMH = relvalue.value+tSMH+tApporder+tsNr;
	dojo.byId("inputTimes").innerHTML = relSMH;
	//dojo.byId("smh_${docMtl.editNodeT.id}").innerHTML=relSMH;
}
////////////////////////////////////////////////
//
// edit expr
//
////////////////////////////////////////////////
function showHtml(idSuffix, innerHTML){
	var htmlO		= dojo.byId("html_"+idSuffix);
	htmlO.innerHTML	= innerHTML;
}
function typeOfDialog(tv){
	var typeOfDialog			= dojo.byId("typeOfDialog");
	typeOfDialog.value=tv;
}
function ruleProcent(){
	typeOfDialog("ruleProcent");
	var inputProcent			= dojo.byId("inputProcent");
	var hidden_inputProcent		= dojo.byId("hidden_inputProcent");
	hidden_inputProcent.value	= inputProcent.value;
	showHtml("selectThenExpr",inputProcent.value+"%");
}
function selectExpr(selectO){
	typeOfDialog("selectExpr");
	var idSuffix = selectO.id;
	var selectValueO= dojo.byId("op_"+selectO.value);
	var hiddenIdO	= dojo.byId("hidden_"+idSuffix);
	hiddenIdO.value	= selectO.value;
	var innerHTML = selectValueO.innerHTML;
	showHtml(idSuffix,innerHTML);
}
////////////////////////////////////////////////
//
// edit notice
//
////////////////////////////////////////////////
var edPl = new Array('viewsource', '|', "bold", "italic","underline","subscript","superscript","|",'pastefromword',"|","createLink","unlink","|");
var edPlExtra = [
				{name: 'dojox.editor.plugins.TablePlugins', command: 'insertTable'},
                 {name: 'dojox.editor.plugins.TablePlugins', command: 'modifyTable'},
                 {name: 'dojox.editor.plugins.TablePlugins', command: 'InsertTableRowBefore'},
                 {name: 'dojox.editor.plugins.TablePlugins', command: 'InsertTableRowAfter'},
                 {name: 'dojox.editor.plugins.TablePlugins', command: 'insertTableColumnBefore'},
                 {name: 'dojox.editor.plugins.TablePlugins', command: 'insertTableColumnAfter'},
                 {name: 'dojox.editor.plugins.TablePlugins', command: 'deleteTableRow'},
                 {name: 'dojox.editor.plugins.TablePlugins', command: 'deleteTableColumn'},
                 {name: 'dojox.editor.plugins.TablePlugins', command: 'colorTableCell'},
                 {name: 'dojox.editor.plugins.TablePlugins', command: 'tableContextMenu'}
                 ];

function htmlEditor4(h,focusOnLoad){
	Spring.addDecoration ( new Spring.ElementDecoration ( {
		elementId:"htmlEditor", widgetType:"dijit.Editor",
		widgetAttrs:
		{
			required : true, focusOnLoad: focusOnLoad,
			height: h,
			plugins: edPl,
			onChange : function(){setNotice();}
		}
	}));
}
function htmlEditor3(h){
	htmlEditor4(h,true);
}
function htmlEditor2(h){
	Spring.addDecoration ( new Spring.ElementDecoration ( {
		elementId:"htmlEditor", widgetType:"dijit.Editor",
		widgetAttrs:
		{
			required : true, focusOnLoad: true,
			height: h,
			plugins: edPl,
			extraPlugins: edPlExtra,
			onChange : function(){setNotice();}
		}
	}));
}
function htmlEditor(){
	Spring.addDecoration ( new Spring.ElementDecoration ( {
		elementId:"htmlEditor", widgetType:"dijit.Editor",
		widgetAttrs:
		{
			required : true, focusOnLoad: true,
			height: "5em",
			//height: "60px",
			onChange : function(){setNotice();}
		}
	}));
}
function setNotice(){
	/*
	console.log("----------------------------");
	console.log(dojo.byId("inputNotice"));
	 * */
	console.log("-----------setNotice()-----------------");
	console.log("----------------------------"+dijit.byId("htmlEditor").getValue());
	console.log("----------------------setNotice()------");
	dojo.byId("inputNotice").value=dijit.byId("htmlEditor").getValue();
}

function labChain(){
	var labChain="";
	var labChainView="";
	var liLabs=dojo.query("ul#labChain1 input");
	for(var i = 0; i < liLabs.length; i++)
		if(liLabs[i].checked)
	{
		labChain+=liLabs[i].value+", ";
		var labl=dojo.byId('lab'+liLabs[i].id);
		labChainView+=labl.innerHTML+", ";
	}
	dojo.byId('labChainView').innerHTML=labChainView;
	dojo.byId('laborChain').value=labChain;
}

////////////////////////////////////////////////
//
// Dojo Tree Section
//
////////////////////////////////////////////////
var storeT;	
function createDataStore(uri)
{
	dojo.require("dojo.data.ItemFileReadStore");
	dojo.require("dijit.Tree");
	console.log("------dojoDataStore.tag------1-------- uri="+uri);
	storeT = new dojo.data.ItemFileReadStore( {url : uri});
	console.log("------dojoDataStore.tag------2-------- storeT="+storeT);
	dojo.addOnLoad
	(
		function()
		{
			dojo.require("dojo.data.ItemFileReadStore");
			dojo.require("dijit.Tree");
			console.log("------dojoDataStore.tag------3-------- uri="+uri);
			storeT = new dojo.data.ItemFileReadStore( {url : uri});
			console.log("------dojoDataStore.tag------4-------- storeT="+storeT);
		}
	);
}

function createForestStoreModel(query)
{
	console.log("createForestStoreModel:query=" +query );
//	var modelT = new dijit.tree.ForestStoreModel
	var modelT = new dijit.tree.TreeStoreModel
	(
		{
			store : storeT,
			query :
			{
				"name" : query
			},
			childrenAttrs : [ "children" ],
			openOnClick : "true",
			showRoot : "false",
			rootId : "root"
		}
	);
	console.log("createForestStoreModel:modelT");
	console.log(modelT);
	return modelT;
}

function createDojoTree(name, query, modelT)
{
	console.log("createDojoTree:name="+name, " query="+query);
	Spring.addDecoration
	(
		new Spring.ElementDecoration
		(
			{
				elementId : name,
				widgetType : "dijit.Tree",
				widgetAttrs :
				{
					persist:true,
					model : modelT,
					//dndController: "dijit._tree.dndSource",
					onClick : function(item, treeNode)
					{
//						self.location.href='${name}?id='+item.id;
						console.log("idFolder:" + item.id);
						//refreshComponent('folderViewer', item.id);
						//do not search  so left the last param blank
						
						//explorerAjaxRequest('folderContent', '${query}', item.id,"",  "searchContentContainer");
						
						
						//compose the url
						//url = 'folderContent?folder=${query}&amp;id='+item.id+'&amp;search=';
//						url = 'explorer?id='+item.id+'&search';
						url = 'explorer='+item.id;
						
						self.location.href=url;
						
						clickInvisibleSpringAjaxLink(url, 'fragContent');
						
					
						if(dojo.byId('pilotTableLink') != null)
							dojo.byId('pilotTableLink').href = '../extra/pilottable?id='+item.id;
						if(dojo.byId('newProtocolLink') != null)
							dojo.byId('newProtocolLink').href = 'explorerflow?idf='+item.id;
						if(dojo.byId('newPatientLink') != null)
							dojo.byId('newPatientLink').href = 'newpatient?idf='+item.id;
						if(dojo.byId('importProtocolLink') != null)
							dojo.byId('importProtocolLink').href = 'import?idf='+item.id;
						if(dojo.byId('idf') != null)
							dojo.byId('idf').value = item.id;
						
					},
					showRoot : "false"
				}
			}
		)//end new ElementDeco
	);//end addDeco
	
	dojo.addOnLoad(function()
	{			
		dojo.connect
		(
			dijit.byId(name).domNode,
			"oncontextmenu", 
			null, 
			function(e)
			{
				//console.log("i=" + i);
				console.log(e.target);
				var el = e.target;
				var w = dijit.getEnclosingWidget(el);
				
				//make impossible to get the menu by clicking the tree container or a tree row
				//console.log(w.isTreeNode);
	//			if (!w.isTreeNode)				
				if(el.getAttribute("dojoattachpoint") == "containerNode" || el.getAttribute("dojoattachpoint") == "rowNode")
				{
					//console.log("hide menu");
					dojo.style("treeMenu", {"display":"none"});
				}
				else
				{
					console.debug("w.item");
					console.debug(w.item);
					dojo.byId("treeMenu").setAttribute("fid", w.item.id);
				}
			}
		);		
	});//addOnLoad
	
}//createDojoTree

///////////////////////////////////////////////////////////////////
//
//	ADD CONTEXT MENU TO ALL Folder Trees
//
///////////////////////////////////////////////////////////////////
function createConceeptContextMenu()
{
	menuItem("newProtocolTCM",	"dijitEditorIcon dijitEditorIconNew",	newProtocolTCM);
}

function createTreeContextMenu()
{	
	menuItem("newNode",			"dijitEditorIcon dijitEditorIconCopy",	newTreeNode);
	menuItem("renameNode",		"dijitEditorIcon dijitEditorIconCopy",	renameTreeNode);
	menuItem("deleteTreeNode",	"dijitEditorIcon dijitEditorIconDelete",deleteTreeNode);
	menuItem("moveUp",			"dijitEditorIcon dijitEditorIconCopy",	moveTreeNodeUp);
	menuItem("moveDown",		"dijitEditorIcon dijitEditorIconCopy",	moveTreeNodeDown);
	
	console.log("createTreeContextMenu");
	
	//build the targetNodeId - list of all folder trees
//	var targetIds= [];
	var targetIds= new Array();
	var ft = dojo.query('.folderTree');
	ft.forEach
	(
		function(node)
		{			
			//console.log(index);
			
			//console.log(node.getAttribute('id'));
			//console.log(node.attr('id'));
			console.log('-----');
			targetIds[targetIds.length] = node.getAttribute('id');	
		}
	);
	console.log(targetIds);
	
	Spring.addDecoration
	(	
		new Spring.ElementDecoration
		(
			{
				elementId:"treeMenu", 
				widgetType:"dijit.Menu", 
				widgetAttrs : { targetNodeIds:targetIds}
			}
		)
	);
}//createTreeContextMenu



////////////////////////////////////////////////
//
// UI Accordion Section
//
////////////////////////////////////////////////
function createAccordion(id, height, width)
{
	Spring.addDecoration
	(
		new Spring.ElementDecoration
		( 
			{
				elementId : id, 
				widgetType : "dijit.layout.AccordionContainer",
				widgetAttrs : 
				{
					style : "width: "+width+"; height: "+height+";"
				}
			}
		)
	);
}

function createAccordionPane(id, isSelected, title)
{
	console.log("createAccordionPane: " + id + "- " + isSelected);
	Spring.addDecoration
	(
		new Spring.ElementDecoration
		( 
			{
				elementId : id,
				widgetModule : "dijit.layout.AccordionContainer", 
				widgetType : "dijit.layout.AccordionPane",
				widgetAttrs : 
				{
					title : title,
					selected:isSelected
				}
			}
		)
	);
}
// CALC TABLET BEGIN
function calcTablet(){
	//console.log("tabletSortNumber ="+tabletSortNumber);
	var tabletGabeSum=0;
	if(tabletSortNumber>0){
		var t0quantity=dojo.byId("t0quantity").value;
		var t0dquantity=dojo.byId("t0dquantity").value;
		tabletGabeSum +=t0value*t0quantity+t0value*t0dquantity/t0divisor;
	}
	if(tabletSortNumber>1){
		var t1quantity=dojo.byId("t1quantity").value;
		var t1dquantity=dojo.byId("t1dquantity").value;
		tabletGabeSum +=t1value*t1quantity+t1value*t1dquantity/t1divisor;
	}
	if(tabletSortNumber>2){
		var t2quantity=dojo.byId("t2quantity").value;
		var t2dquantity=dojo.byId("t2dquantity").value;
		tabletGabeSum +=t2value*t2quantity+t2value*t2dquantity/t2divisor;
	}
	var p=(tabletGabeSum-calcDose)/calcDose*100;
	p=Math.round(p);
	var title =""+p+"%≅"+tabletGabeSum+"↔"+calcDose;
	var tSum=dojo.byId("tSum");
	tSum.innerHTML=tabletGabeSum;
	tSum.title=title;
	dojo.byId("tSumTitle").innerHTML=title;
	//alert("Hallo "+tabletGabeSum);
	//console.log("tabletGabeSum ="+tabletGabeSum);
}
// CALC TABLET END
function protocol2schemaName(pName){
//	alert("123"+pName.value);
	var schemaNameO=dojo.byId("newProtocol.schemaName");
	schemaNameO.value=pName.value;
}


function addToolbarButton(buttonId, buttonLabel, elementsToClick)
{
//    dojo.require("dijit.Toolbar");
//    dojo.require("dijit.form.Button");
    dojo.ready(function()
	{
        //toolbar = new dijit.Toolbar({}, "toolbar");
        var t = dijit.byId(toolbarId);
        //console.log(t);
        
        var button = new dijit.form.Button
        (	
        	{
	            id: buttonId,
	            type:'submit',
	            label: buttonLabel,
	            showLabel: true,
	            iconClass: "owsIcon owsIcon"+buttonId,
	            onClick: function()
	            {
					dojo.forEach
					(
						elementsToClick,
						function(element)
						{
							dojo.byId(element).click();
						}
					);
	            }
        	}, 
        	buttonId
        );
       t.addChild(button);
       
//        dojo.forEach
//        (
//        		["Save"], 
//        		function(label)
//        		{
//		            var button = new dijit.form.Button({
//		                id: buttonId,
//		                label: buttonLabel,
//		                showLabel: true,
//		                iconClass: "owsIcon owsIcon"+label
//		            }, buttonId);
//		  			//button.set('disabled', true);
//		           t.addChild(button);
//        		}
//       );
      //  console.log(t.getChildren());
    });
}

function disableToolBarButton(buttonId, enableState)
{
	dojo.ready(function(){
		//var t = dijit.byId(toolbarId);
		//console.log(t);
		var b = dijit.byId(buttonId);
		//console.log(b);
		b.set('disabled', enableState);
	});
}

/*******************************************************************************
 * 
 * 	Section: Checkbox for input whether float or int into DoseValue Combobox
 *  (siehe dose.tagx)
 *  
 ******************************************************************************/
function validateElement(elementId)
{
	console.log(elementId);
	var dEl=dijit.byId(elementId);
	console.log(dEl);
	dEl.validate();
}

function setupChooseFloatIntCheckbox()
{
	console.log("setupChooseFloatIntCheckbox");
	var value = dijit.byId("doseValue").get('value');
	console.log(value);
	if(!isFloatPartEmpty(value))
		dojo.attr( "chooseFloatInt", "checked", "checked");
	else
		float2int("doseValue");
}

function isFloat(str)
{	
	return (str.indexOf('.') >0) ?true:false;
}

function isFloatPartEmpty(str)
{
	var num = parseFloat(str);
   	
	var intpart = parseInt(num);
	var decpart = num - intpart;
	console.log("Num = %f, intpart = %d, decpart = %f\n", num, intpart, decpart);
	return (decpart>0) ? false:true;
}

function float2intE(element)
{
	var intValue = parseInt(element.get('value'));
	element.set('value',intValue);	
	return element;
}

function int2floatE(element)
{
	var n = new Number(parseInt(element.get('value')));
	var f = n.toFixed(1);
	element.set('value',f);	
	return element;
}

function float2int(elementId)
{
	var element = dijit.byId(elementId);
	var intValue = parseInt(element.get('value'));
	element.set('value',intValue);	
	return element;
}

function int2float(elementId)
{
	var element = dijit.byId(elementId);
	var f = parseFloat(element.get('value'));
	element.set('value',f);	
	return element;
}


