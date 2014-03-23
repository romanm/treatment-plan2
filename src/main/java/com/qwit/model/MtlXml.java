package com.qwit.model;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.qwit.domain.MObject;
import com.qwit.domain.Tree;

public class MtlXml {
	protected final Log log = LogFactory.getLog(getClass());
	private Document xmlDoc;
	Map<Integer, MObject> classM ;
	Map<Integer, String> folderNM ;
	Tree docT;
	public MtlXml(TreeManager schemaMtl) {
		classM = schemaMtl.getClassM();
		docT=schemaMtl.getDocT();
	}
	public void setFolderNM(Map<Integer, String> folderNM) {this.folderNM = folderNM;}
	public String getXml(){return xmlDoc.getRootElement().asXML();}
	public Document makeTreeDoc2xml(){return treeDoc2xml(docT);}
	public Document treeDoc2xml(Tree tree){
		xmlDoc = DocumentHelper.createDocument();
		Element el = xmlDoc.addElement(tree.getTabName());
		tree2xml(tree,el,tree);
		return xmlDoc;
	}
	public void tree2xml(Tree tree,Element el,Tree treeDoc){
		el.addAttribute("id", tree.getId().toString());
		if(tree.getSort()!=null)
			el.addAttribute("sort", tree.getSort().toString());
		if(tree.getRef()!=null)
			el.addAttribute("ref", tree.getRef().toString());
		if(tree.getIdClass()!=null){
//			log.debug(tree);
//			log.debug(schemaMtl.getClassM().get(tree.getIdClass()));
			el.addAttribute("idclass", tree.getIdClass().toString());
			if(classM.get(tree.getIdClass())!=null){
				el.addText(classM.get(tree.getIdClass()).toString());
			}else if(folderNM!=null){
				String string = folderNM.get(tree.getIdClass());
				el.addText(string);
			}
		}
		if(tree.getHistory()!=null){
			el.addAttribute("mdate", tree.getHistory().getMdate().toString());
//			el.addAttribute("mdate", tree.getHistory().getMdate().toGMTString());
			el.addAttribute("idauthor", tree.getHistory().getIdauthor().toString());
		}
		if(tree.getId().equals(tree.getIdClass())
				&&!tree.getId().equals(tree.getDocT().getId())
				&&!tree.getTabName().equals(tree.getTabName())
			)
			return;
		for(Tree t:tree.getChildTs())
			if(t.getDocT()==treeDoc&&9930!=t.getId())
				tree2xml(t,el.addElement(t.getTabName()),treeDoc);
	}

}
