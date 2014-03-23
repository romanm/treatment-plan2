////////////////////////////////////////////////
//
//  dojo Menu system
//
////////////////////////////////////////////////
var menues		= Object();
var menubars	= Object();

function createMenu(menuName, toolbarId)
{
 	dojo.require("dijit.Toolbar");
	dojo.require("dijit.form.Button");
	dojo.require("dijit.Menu");
	
	//console.log("menues");
	if(menues[toolbarId] == null)
		menues[toolbarId] = Object();
	menues[toolbarId][menuName] = new dijit.Menu({ style: "display: none;" });
	if(menues[toolbarId][menuName]['menuname'] == null)
		menues[toolbarId][menuName]['menuname'] = Object();
	menues[toolbarId][menuName]['menuname'] = menuName;
	//console.log(menues[toolbarId][menuName]);
}

function createToolbar(toolbarId)
{
	console.log("createToolbar" + toolbarId);
	menubars[toolbarId] = new dijit.Toolbar({}, toolbarId);
	//console.log(menubars[toolbarId]);
	//console.log(menubars[toolbarId]);
	for(var i in menues[toolbarId])
	{
		console.log("i=" + i);
		var button = new dijit.form.DropDownButton({label: menues[toolbarId][i]['menuname'],  dropDown:  menues[toolbarId][i] });
		//console.log(menues[toolbarId][i]);
		menubars[toolbarId].addChild(button);
	}
	menubars[toolbarId].startup();
}

function editTest()
{
	console.log("menu selected");
}

function findClickLink(cssSelector){
	dojo.query(cssSelector).forEach(function(link){
		if(link.hasAttribute("onclick")){
			dojo.eval(link.getAttribute("onclick"));
		}else{
			self.location.href=link.href;
		}
		return;
	});
}
function addMenuItem2(toolbarId, menuName, mlabel,classes,cssSelector){
	menues[toolbarId][menuName].addChild (new dijit.MenuItem({
		label: mlabel,
		iconClass:classes,
		onClick: function(){findClickLink(cssSelector);}
	}));
}
function addMenuItem(toolbarId, menuName, mlabel,classes,click)
{
//	console.log("addMenuItem: " +toolbarId +" "+ menuName+" "+ mlabel+" "+classes);
//	console.log(menues[toolbarId][menuName]);
	menues[toolbarId][menuName].addChild(
		new dijit.MenuItem
		(
			{
				label: mlabel,
				iconClass:classes,
				onClick: click
			}
		)
	);
}

function addSubMenuItem(toolbarId, menuName, parentLabel, mlabel,classes,click)
{
//	console.log("Sub - Item: " +toolbarId +" "+ menuName+" "+ mlabel+" "+classes);
//	console.log(menues[toolbarId][menuName]);
	var msub = new dijit.Menu();
	msub.addChild(new dijit.MenuItem({ label: mlabel, onClick:click}));
//	console.log("Sub");
	menues[toolbarId][menuName].addChild
	(
		new dijit.PopupMenuItem
		(
			{
				label: parentLabel,
				iconClass:classes,//"dijitEditorIcon dijitEditorIconCopy",
				popup: msub
			}
		)
	);
	
	//console.log(menues[toolbarId][menuName].getChildren());
}

