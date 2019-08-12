package com.dotcms.contenttype.model.field.layout;

import com.dotcms.contenttype.model.field.*;
import com.dotcms.contenttype.model.type.ContentType;
import com.dotcms.contenttype.transform.contenttype.ContentTypeInternationalization;
import com.dotcms.contenttype.transform.field.JsonFieldTransformer;
import com.dotcms.util.CollectionsUtils;
import com.dotcms.util.IntegrationTestInitService;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.liferay.portal.model.User;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@PrepareForTest(FieldUtil.class)
@RunWith(PowerMockRunner.class)
public class FieldLayoutColumnSerializerTest {

    @BeforeClass
    public static void prepare() throws Exception{
        IntegrationTestInitService.getInstance().init();
    }

    @Test()
    public void testSerialize() throws IOException {
        final ColumnField columDivider =  ImmutableColumnField.builder()
                .name("Column Field 1")
                .sortOrder(0)
                .build();

        final List<Field> fields = CollectionsUtils.list(
                ImmutableTextField.builder()
                        .name("Text Field")
                        .sortOrder(2)
                        .build()
        );

        final FieldLayoutColumn fieldLayoutColumn = new FieldLayoutColumn(columDivider, fields);

        final JsonGenerator jsonGenerator = mock(JsonGenerator.class);
        final SerializerProvider serializerProvider = mock(SerializerProvider.class);

        final FieldLayoutColumnSerializer fieldLayoutColumnSerializer = new FieldLayoutColumnSerializer();
        fieldLayoutColumnSerializer.serialize(fieldLayoutColumn, jsonGenerator, serializerProvider);

        verify(jsonGenerator).writeStartObject();

        final JsonFieldTransformer jsonFieldDividerTransformer =
                new JsonFieldTransformer(fieldLayoutColumn.getColumn());
        verify(jsonGenerator).writeObjectField("columnDivider", jsonFieldDividerTransformer.mapObject());

        final JsonFieldTransformer jsonColumnsTransformer = new JsonFieldTransformer(fieldLayoutColumn.getFields());
        verify(jsonGenerator).writeObjectField("fields", jsonColumnsTransformer.mapList());

        verify(jsonGenerator).writeEndObject();
        verify(jsonGenerator).flush();
    }

    @Test()
    public void testSerializeWhenPassContentTypeInternationalization() throws IOException, DotDataException, DotSecurityException {
        final ContentType contactUs = APILocator.getContentTypeAPI(APILocator.systemUser()).find("ContactUs");

        final long languageId = 1;
        final boolean live = true;
        final User user = mock(User.class);
        final ContentTypeInternationalization contentTypeInternationalization =
                new ContentTypeInternationalization(languageId, live, user);

        final ColumnField columDivider =  ImmutableColumnField.builder()
                .name("Column Field 1")
                .sortOrder(0)
                .build();

        final List<Field> fields = CollectionsUtils.list(
                ImmutableTextField.builder()
                        .name("Text Field")
                        .sortOrder(2)
                        .build()
        );

        final FieldLayoutColumn fieldLayoutColumn = new FieldLayoutColumn(columDivider, fields);

        final JsonGenerator jsonGenerator = mock(JsonGenerator.class);

        final SerializerProvider serializerProvider = mock(SerializerProvider.class);
        when(serializerProvider.getAttribute("internationalization")).thenReturn(contentTypeInternationalization);
        when(serializerProvider.getAttribute("type")).thenReturn(contactUs);

        final JsonFieldTransformer jsonFieldTransformer = new JsonFieldTransformer(fields.get(0));
        final Map<String, Object> fieldMap = jsonFieldTransformer.mapObject();

        PowerMockito.mockStatic(FieldUtil.class);

        final FieldLayoutColumnSerializer fieldLayoutColumnSerializer = new FieldLayoutColumnSerializer();
        fieldLayoutColumnSerializer.serialize(fieldLayoutColumn, jsonGenerator, serializerProvider);

        verify(jsonGenerator).writeStartObject();

        final JsonFieldTransformer jsonFieldDividerTransformer =
                new JsonFieldTransformer(fieldLayoutColumn.getColumn());
        verify(jsonGenerator).writeObjectField("columnDivider", jsonFieldDividerTransformer.mapObject());

        final JsonFieldTransformer jsonColumnsTransformer = new JsonFieldTransformer(fieldLayoutColumn.getFields());
        verify(jsonGenerator).writeObjectField("fields", jsonColumnsTransformer.mapList());

        verify(jsonGenerator).writeEndObject();
        verify(jsonGenerator).flush();

        PowerMockito.verifyStatic();
        FieldUtil.setFieldInternationalization(contactUs, contentTypeInternationalization, fieldMap);
    }
}
