package com.qwit.tests.flow;

import org.easymock.EasyMock;
import org.springframework.webflow.config.FlowDefinitionResource;
import org.springframework.webflow.config.FlowDefinitionResourceFactory;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.test.MockExternalContext;
import org.springframework.webflow.test.MockFlowBuilderContext;
import org.springframework.webflow.test.MockParameterMap;
import org.springframework.webflow.test.execution.AbstractXmlFlowExecutionTests;

import com.qwit.serviceinterface.IExplorerService;
import com.qwit.serviceinterface.IProtocolService;

public class SchemaFlowExecutionTests extends AbstractXmlFlowExecutionTests
{

    private IProtocolService  protocolService;
    private IExplorerService  explorerService;
//    @Autowired ExplorerService explorerService;

    protected void setUp() 
    {
		//schemaService = EasyMock.createMock(SchemaService.class);
		protocolService = EasyMock.createMock(IProtocolService.class);
		explorerService = EasyMock.createMock(IExplorerService.class);
		
    }
    
    @Override
    protected FlowDefinitionResource getResource(FlowDefinitionResourceFactory resourceFactory) {
	return resourceFactory.createFileResource("src/main/webapp/WEB-INF/explorerflow/explorer-flow.xml");
    }

    @Override
    protected void configureFlowBuilderContext(MockFlowBuilderContext builderContext) 
    {
//    	builderContext.registerBean("schemaService", schemaService);
    	builderContext.registerBean("protocolService", protocolService);
    	builderContext.registerBean("explorerService", explorerService);
    }

    
    //test creation of a new protocol in an existing folder   
//    public void testSaveNewProtocol()
//    {
//    	
//    	FlowObjCreator foc = new FlowObjCreator();
//		foc.setIdf(1265938);
//		foc.getNewProtocol().setProtocol("Test Protocol");
//    	protocolService.saveNewProtocol(foc);
//    }
    
	public void testMakeExplorerMtl()
	{
//		System.out.println("test");
//		ExplorerMtl e = explorerService.getExplorerMtl();
//		ExplorerMtl e = explorerService.makeExplorerMtl(78219, "search");
//		System.out.println("testMakeExplorerMtl:e:"+e);
//		assertTrue(e != null);
		
	}
    
//    public void testMakeExplorerMtl()
//    {
//    	ExplorerMtl explorerMtl = new ExplorerMtl("folder");
//    	assertTrue(explorerMtl != null);
//    }
//    public void testMakeExplorerMtl2()
//    {
//    	ExplorerMtl e = explorerService.makeExplorerMtl(78219, "search");
//    	assertTrue(e != null);
//    	assertTrue(e instanceof ExplorerMtl);    	
//    }
    
    
    public void testStartExplorerFlow() {

        MutableAttributeMap input = new LocalAttributeMap();
//        input.put("id", "1");
//        input.put("idc", "1");
//        input.put("a", "newProtocol");
//        input.put("folder", "test");
//        input.put("search", "test");
    	
        MockParameterMap requestParameterMap = new MockParameterMap();
        requestParameterMap.put("id", "78219");
        requestParameterMap.put("idc", "78219");
        requestParameterMap.put("a", "newProtocol");
        requestParameterMap.put("folder", "test");
        requestParameterMap.put("search", "test");
        
        MockExternalContext context = new MockExternalContext();
        context.setRequestParameterMap(requestParameterMap );
        context.setCurrentUser("roman");
//        getFlowScope().put("explorerMtl", explorerService.makeExplorerMtl(78219, "search"));
        startFlow(input, context);
//        assertCurrentStateEquals("saveNewProtocol");
    }

//    public void testStartSchemaFlow() {
//    	setCurrentState("saveNewProtocol");
//    	//getFlowScope().put("booking", createTestBooking());
//    	FlowObjCreator foc = new FlowObjCreator();
//		foc.setIdf(1265938);
//		foc.getNewProtocol().setProtocol("Test Protocol");
//    	getFlowScope().put("flowObjCreator", foc);
//    	
////    	FlowObjCreator foc = (FlowObjCreator) getFlowScope().get("flowObjCreator");    	
////    	FlowObjCreator f = (FlowObjCreator) getFlowAttribute("flowObjCreator");
////    	assertTrue(f != null);
////    	assertTrue(f.getNewProtocol() != null);
////    	foc.getNewProtocol().setProtocol("Test Protocol");
////    	
//    	
//    	MockExternalContext context = new MockExternalContext();
//    	context.setEventId("save");
//    	resumeFlow(context);
//    	assertFlowExecutionEnded();
//    	assertFlowExecutionOutcomeEquals("saveNewDoc");
//    }
    
        
        
}
