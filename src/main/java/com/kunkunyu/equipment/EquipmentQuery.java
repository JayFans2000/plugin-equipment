package com.kunkunyu.equipment;

import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static run.halo.app.extension.router.QueryParamBuildUtil.sortParameter;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ServerWebExchange;
import run.halo.app.extension.router.IListRequest;
import run.halo.app.extension.router.SortableRequest;

/**
 * A query object for {@link Equipment} list.
 *
 */
public class EquipmentQuery extends SortableRequest {

    public EquipmentQuery(ServerWebExchange exchange) {
        super(exchange);
    }

    @Schema(description = "Equipments filtered by group.")
    public String getGroup() {
        return queryParams.getFirst("group");
    }

    @Nullable
    @Schema(description = "Equipments filtered by keyword.")
    public String getKeyword() {
        return queryParams.getFirst("keyword");
    }

    public static void buildParameters(Builder builder) {
        IListRequest.buildParameters(builder);
        builder.parameter(sortParameter())
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("keyword")
                .description("Equipments filtered by keyword.")
                .implementation(String.class)
                .required(false))
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("group")
                .description("equipment group name")
                .implementation(String.class)
                .required(false));
    }
}
