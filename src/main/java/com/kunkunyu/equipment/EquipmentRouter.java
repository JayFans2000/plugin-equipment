package com.kunkunyu.equipment;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static run.halo.app.theme.router.PageUrlUtils.totalPage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;

import com.kunkunyu.equipment.finders.EquipmentFinder;
import com.kunkunyu.equipment.vo.EquipmentGroupVo;
import com.kunkunyu.equipment.vo.EquipmentVo;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.ReactiveSettingFetcher;
import run.halo.app.theme.TemplateNameResolver;
import run.halo.app.theme.router.PageUrlUtils;
import run.halo.app.theme.router.UrlContextListResult;

/**
 * Provides a <code>/equipments</code> route for the topic end to handle routing.
 *
 */
@Component
@AllArgsConstructor
public class EquipmentRouter {
    
    private static final String GROUP_PARAM = "group";
    
    private EquipmentFinder equipmentFinder;
    
    private final ReactiveSettingFetcher settingFetcher;

    private final TemplateNameResolver templateNameResolver;
    
    /**
     * Provides a <code>/equipments</code> route for the topic end to handle routing.
     *
     * @return a {@link RouterFunction} instance
     */
    @Bean
    RouterFunction<ServerResponse> equipmentRouter() {
        return route(GET("/equipments").or(GET("/equipments/page/{page:\\d+}")),
            handlerFunction()
        );
    }
    
    private HandlerFunction<ServerResponse> handlerFunction() {
        return request -> templateNameResolver.resolveTemplateNameOrDefault(request.exchange(), "equipments")
            .flatMap(templateName -> ServerResponse.ok().render(templateName,
                Map.of("groups", equipmentGroups(),
                    "equipments", equipmentList(request),
                    ModelConst.TEMPLATE_ID, templateName,
                    "title", getEquipmentTitle(),
                    "equipmentName", EquipmentPage.SINGLETON_NAME
                ))
            );
    }
    
    private Mono<UrlContextListResult<EquipmentVo>> equipmentList(ServerRequest request) {
        String path = request.path();
        int pageNum = pageNumInPathVariable(request);
        String group = groupPathQueryParam(request);
        return this.settingFetcher.get("base")
            .map(item -> item.get("pageSize").asInt(10))
            .defaultIfEmpty(10)
            .flatMap(pageSize -> equipmentFinder.list(pageNum, pageSize, group)
                .map(list -> new UrlContextListResult.Builder<EquipmentVo>()
                    .listResult(list)
                    .nextUrl(appendGroupParam(
                        PageUrlUtils.nextPageUrl(path, totalPage(list)), group)
                    )
                    .prevUrl(appendGroupParam(PageUrlUtils.prevPageUrl(path), group))
                    .build()
                )
            );
        
    }
    
    private static String appendGroupParam(String path, String group) {
        return UriComponentsBuilder.fromPath(path)
            .queryParamIfPresent(GROUP_PARAM, Optional.ofNullable(group))
            .build()
            .toString();
    }
    
    private int pageNumInPathVariable(ServerRequest request) {
        String page = request.pathVariables().get("page");
        return NumberUtils.toInt(page, 1);
    }
    
    private String groupPathQueryParam(ServerRequest request) {
        return request.queryParam(GROUP_PARAM)
            .filter(StringUtils::isNotBlank)
            .orElse(null);
    }
    
    Mono<String> getEquipmentTitle() {
        return this.settingFetcher.get("base").map(
            setting -> setting.get("title").asText("装备")).defaultIfEmpty(
            "装备");
    }
    
    private Mono<List<EquipmentGroupVo>> equipmentGroups() {
        return equipmentFinder.groupBy().collectList();
    }
}
