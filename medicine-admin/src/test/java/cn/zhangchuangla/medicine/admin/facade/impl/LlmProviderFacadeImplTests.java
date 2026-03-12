package cn.zhangchuangla.medicine.admin.facade.impl;

import cn.zhangchuangla.medicine.admin.model.request.*;
import cn.zhangchuangla.medicine.admin.service.LlmProviderModelService;
import cn.zhangchuangla.medicine.admin.service.LlmProviderService;
import cn.zhangchuangla.medicine.model.entity.LlmProvider;
import cn.zhangchuangla.medicine.model.entity.LlmProviderModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LlmProviderFacadeImplTests {

    @Mock
    private LlmProviderService llmProviderService;

    @Mock
    private LlmProviderModelService llmProviderModelService;

    @InjectMocks
    private LlmProviderFacadeImpl llmProviderFacade;

    @Test
    void getProviderDetail_ShouldAggregateProviderAndModels() {
        when(llmProviderService.getRequiredProvider(1L)).thenReturn(buildProvider());
        when(llmProviderModelService.listProviderModels(1L))
                .thenReturn(List.of(buildProviderModel("gpt-4.1", "CHAT")));

        var detail = llmProviderFacade.getProviderDetail(1L);

        assertEquals("OpenAI", detail.getProviderName());
        assertEquals("openai", detail.getProviderType());
        assertEquals("sk-detail", detail.getApiKey());
        assertEquals(1, detail.getModels().size());
    }

    @Test
    void createProvider_ShouldSaveProviderThenModels() {
        LlmProviderCreateRequest request = new LlmProviderCreateRequest();
        request.setModels(List.of(buildRequestModel("gpt-4.1", "CHAT")));
        when(llmProviderService.createProvider(request)).thenReturn(buildProvider());
        when(llmProviderModelService.saveProviderModels(any(), any(), anyString())).thenReturn(true);

        boolean result = llmProviderFacade.createProvider(request);

        assertTrue(result);
        var inOrder = inOrder(llmProviderService, llmProviderModelService);
        inOrder.verify(llmProviderService).createProvider(request);
        inOrder.verify(llmProviderModelService).saveProviderModels(any(), any(), anyString());
    }

    @Test
    void updateProvider_ShouldReplaceModelsAfterProviderUpdate() {
        LlmProviderUpdateRequest request = new LlmProviderUpdateRequest();
        request.setId(1L);
        request.setModels(List.of(buildRequestModel("gpt-4.1", "CHAT")));
        when(llmProviderService.updateProvider(request)).thenReturn(buildProvider());
        when(llmProviderModelService.remove(any())).thenReturn(true);
        when(llmProviderModelService.saveProviderModels(any(), any(), anyString())).thenReturn(true);

        boolean result = llmProviderFacade.updateProvider(request);

        assertTrue(result);
        var inOrder = inOrder(llmProviderService, llmProviderModelService);
        inOrder.verify(llmProviderService).updateProvider(request);
        inOrder.verify(llmProviderModelService).remove(any());
        inOrder.verify(llmProviderModelService).saveProviderModels(any(), any(), anyString());
    }

    @Test
    void deleteProvider_ShouldRemoveModelsBeforeDeletingProvider() {
        when(llmProviderService.getRequiredProvider(1L)).thenReturn(buildProvider());
        when(llmProviderModelService.remove(any())).thenReturn(true);
        when(llmProviderService.deleteProvider(1L)).thenReturn(true);

        boolean result = llmProviderFacade.deleteProvider(1L);

        assertTrue(result);
        var inOrder = inOrder(llmProviderService, llmProviderModelService);
        inOrder.verify(llmProviderService).getRequiredProvider(1L);
        inOrder.verify(llmProviderModelService).remove(any());
        inOrder.verify(llmProviderService).deleteProvider(1L);
    }

    @Test
    void listProviderModels_ShouldValidateProviderBeforeQuery() {
        when(llmProviderService.getRequiredProvider(1L)).thenReturn(buildProvider());
        when(llmProviderModelService.listProviderModels(1L))
                .thenReturn(List.of(buildProviderModel("gpt-4.1", "CHAT")));

        List<LlmProviderModel> models = llmProviderFacade.listProviderModels(1L);

        assertEquals(1, models.size());
        var inOrder = inOrder(llmProviderService, llmProviderModelService);
        inOrder.verify(llmProviderService).getRequiredProvider(1L);
        inOrder.verify(llmProviderModelService).listProviderModels(1L);
    }

    @Test
    void createProviderModel_ShouldLoadProviderThenSaveModel() {
        LlmProviderModelCreateRequest request = new LlmProviderModelCreateRequest();
        request.setProviderId(1L);
        request.setModelName("gpt-4.1");
        request.setModelType("CHAT");
        when(llmProviderService.getRequiredProvider(1L)).thenReturn(buildProvider());
        when(llmProviderModelService.createProviderModel(any(), any())).thenReturn(true);

        boolean result = llmProviderFacade.createProviderModel(request);

        assertTrue(result);
        var inOrder = inOrder(llmProviderService, llmProviderModelService);
        inOrder.verify(llmProviderService).getRequiredProvider(1L);
        inOrder.verify(llmProviderModelService).createProviderModel(any(), any());
    }

    @Test
    void updateProviderModel_ShouldLoadProviderThenUpdateModel() {
        LlmProviderModelUpdateRequest request = new LlmProviderModelUpdateRequest();
        request.setId(1L);
        request.setProviderId(1L);
        request.setModelName("gpt-4.1");
        request.setModelType("CHAT");
        when(llmProviderService.getRequiredProvider(1L)).thenReturn(buildProvider());
        when(llmProviderModelService.updateProviderModel(any(), any())).thenReturn(true);

        boolean result = llmProviderFacade.updateProviderModel(request);

        assertTrue(result);
        var inOrder = inOrder(llmProviderService, llmProviderModelService);
        inOrder.verify(llmProviderService).getRequiredProvider(1L);
        inOrder.verify(llmProviderModelService).updateProviderModel(any(), any());
    }

    private LlmProvider buildProvider() {
        return LlmProvider.builder()
                .id(1L)
                .providerName("OpenAI")
                .providerType("openai")
                .apiKey("sk-detail")
                .createBy("tester")
                .updateBy("tester")
                .build();
    }

    private LlmProviderModel buildProviderModel(String modelName, String modelType) {
        return LlmProviderModel.builder()
                .id(1L)
                .providerId(1L)
                .modelName(modelName)
                .modelType(modelType)
                .build();
    }

    private LlmProviderModelItemRequest buildRequestModel(String modelName, String modelType) {
        LlmProviderModelItemRequest request = new LlmProviderModelItemRequest();
        request.setModelName(modelName);
        request.setModelType(modelType);
        return request;
    }
}
