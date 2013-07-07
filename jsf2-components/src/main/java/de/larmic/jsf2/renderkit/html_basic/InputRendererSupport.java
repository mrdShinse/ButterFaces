package de.larmic.jsf2.renderkit.html_basic;

import java.io.IOException;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;

import de.larmic.jsf2.component.html.HtmlInputComponent;

/**
 * Renderer support classes provides methods used by custom component
 * converters. Class is non static because of allowing overriding by other
 * custom components.
 */
public class InputRendererSupport {

	private static final String LABEL_STYLE_CLASS = "larmic-component-label";
	private static final String REQUIRED_SPAN_CLASS = "larmic-component-required";
	private static final String INPUT_STYLE_CLASS = "larmic-component-input";
	private static final String COMPONENT_INVALID_STYLE_CLASS = "larmic-input-invalid";
	private static final String INVALID_STYLE_CLASS = "input-invalid";
	private static final String TOOLTIP_CLASS = "larmic-component-tooltip";
	private static final String TOOLTIP_LABEL_CLASS = "larmic-component-label-tooltip";
	private static final String ERROR_MESSAGE_CLASS = "larmic-component-error-message";

	private static final String FLOATING_STYLE = "display: inline-block;";
	private static final String NON_FLOATING_STYLE = "display: table;";

	private static final String TOOLTIP_DIV_CLIENT_ID_POSTFIX = "_tooltip";

	/**
	 * Render outer div and label (if needed) and initializes input component.
	 * 
	 * NOTE: encodeBegin of super implementation should be called first.
	 */
	public void encodeBegin(final FacesContext context, final HtmlInputComponent component) throws IOException {
		final UIInput uiComponent = (UIInput) component;

		final String style = component.getStyle();
		final String styleClass = component.getStyleClass();
		final boolean readonly = component.getReadonly();
		final boolean required = component.isRequired();
		final boolean floating = component.getFloating();
		final boolean valid = component.isValid();
		final String label = component.getLabel();
		final Object value = component.getValue();

		final ResponseWriter writer = context.getResponseWriter();

		writer.startElement("div", uiComponent);
		this.initOuterDiv(style, styleClass, floating, valid, writer);
		this.writeLabelIfNecessary(component, readonly, required, label, writer);

		if (readonly) {
			writer.writeText(this.getReadonlyDisplayValue(value, uiComponent, uiComponent.getConverter()), null);
		}

		this.initInputComponent(uiComponent);
	}

	/**
	 * Render tooltip and closes outer div.
	 * 
	 * NOTE: getEndTextToRender of super implementation should be called first.
	 */
	public void getEndTextToRender(final FacesContext context, final HtmlInputComponent component,
			final String currentValue) throws IOException {
		final UIInput uiComponent = (UIInput) component;
		final ResponseWriter writer = context.getResponseWriter();

		final boolean tooltipNecessary = this.isTooltipNecessary(component);

		if ((tooltipNecessary || !component.isValid()) && !component.getReadonly()) {
			writer.startElement("span", uiComponent);
			writer.writeAttribute("id", uiComponent.getId() + TOOLTIP_DIV_CLIENT_ID_POSTFIX, null);
			writer.writeAttribute("class", TOOLTIP_CLASS, null);
			writer.writeAttribute("style", this.createOuterTooltipStyle(), null);

			if (tooltipNecessary) {
				writer.startElement("span", uiComponent);
				writer.writeAttribute("style", "position:relative;left:5px;top:4px;", null);
				writer.writeText(component.getTooltip(), null);
				writer.endElement("span");
			}

			if (!context.getMessageList().isEmpty()) {
				writer.startElement("div", uiComponent);
				writer.writeAttribute("class", ERROR_MESSAGE_CLASS, null);
				writer.startElement("ul", uiComponent);
				for (final FacesMessage message : context.getMessageList()) {
					writer.startElement("li", uiComponent);
					writer.writeText(message.getSummary(), null);
					writer.endElement("li");
				}
				writer.endElement("ul");
				writer.endElement("div");
			}
			writer.endElement("span");
		}

		writer.endElement("div");
	}

	/**
	 * Should return value string for the readonly view mode. Can be overridden
	 * for custom components.
	 */
	protected String getReadonlyDisplayValue(final Object value, final UIInput component, final Converter converter) {
		if (value == null) {
			return "-";
		} else if (converter != null) {
			return converter.getAsString(FacesContext.getCurrentInstance(), component, value);
		}

		return String.valueOf(value);
	}

	protected void initInputComponent(final UIInput component) {
		component.getAttributes().put(
				"onfocus",
				"document.getElementById('" + component.getId() + TOOLTIP_DIV_CLIENT_ID_POSTFIX
						+ "').style.display = 'inline-block';");
		component.getAttributes().put(
				"onblur",
				"document.getElementById('" + component.getId() + TOOLTIP_DIV_CLIENT_ID_POSTFIX
						+ "').style.display = 'none';");
		if (!component.isValid()) {
			component.getAttributes().put("styleClass", INPUT_STYLE_CLASS + " " + INVALID_STYLE_CLASS);
		} else {
			component.getAttributes().put("styleClass", INPUT_STYLE_CLASS);
		}
	}

	protected void initOuterDiv(final String style, final String styleClass, final boolean floating,
			final boolean valid, final ResponseWriter writer) throws IOException {
		if (styleClass != null) {
			final String clearedStyleClass = styleClass.replaceAll(INVALID_STYLE_CLASS, "");
			writer.writeAttribute("class",
					valid ? clearedStyleClass : this.concatStyles(COMPONENT_INVALID_STYLE_CLASS, clearedStyleClass),
					null);
		} else if (!valid) {
			writer.writeAttribute("class", COMPONENT_INVALID_STYLE_CLASS, null);
		}
		if (style != null) {
			final String floatingStyle = floating ? FLOATING_STYLE : NON_FLOATING_STYLE;
			writer.writeAttribute("style", this.concatStyles(floatingStyle, style), null);
		} else {
			writer.writeAttribute("style", floating ? FLOATING_STYLE : NON_FLOATING_STYLE, null);
		}
	}

	protected void writeLabelIfNecessary(final HtmlInputComponent component, final boolean readonly,
			final boolean required, final String label, final ResponseWriter writer) throws IOException {
		if (label != null) {
			final UIInput uiComponent = (UIInput) component;

			writer.startElement("label", uiComponent);
			if (!readonly) {
				writer.writeAttribute("for", uiComponent.getId(), null);
			}
			if (component.getTooltip() != null && !"".equals(component.getTooltip())) {
				writer.writeAttribute("class", this.concatStyles(LABEL_STYLE_CLASS, TOOLTIP_LABEL_CLASS), null);
			} else {
				writer.writeAttribute("class", LABEL_STYLE_CLASS, null);
			}
			writer.writeAttribute("style", "display: inline-block; margin-right: 2px; float: left;", null);

			writer.startElement("abbr", uiComponent);
			if (this.isTooltipNecessary(component)) {
				writer.writeAttribute("title", component.getTooltip(), null);
				writer.writeAttribute("style", "cursor: help; word-wrap: breaking-word;", null);
			}
			writer.writeText(component.getLabel(), null);
			writer.endElement("abbr");

			this.writeRequiredSpanIfNecessary(component.getClientId(), readonly, required, writer);

			writer.endElement("label");
		}
	}

	protected void writeRequiredSpanIfNecessary(final String clientId, final boolean readonly, final boolean required,
			final ResponseWriter writer) throws IOException {
		if (required && !readonly) {
			writer.startElement("span", null);
			writer.writeAttribute("id", clientId + "_requiredLabel", null);
			writer.writeAttribute("class", REQUIRED_SPAN_CLASS, null);
			writer.writeAttribute("style", "margin-left: 2px; color: red;", null);
			writer.writeText("*", null);
			writer.endElement("span");
		}
	}

	protected String concatStyles(final String... styles) {
		final StringBuilder sb = new StringBuilder();

		for (final String style : styles) {
			sb.append(style);
			sb.append(" ");
		}

		return sb.toString();
	}

	protected boolean isTooltipNecessary(final HtmlInputComponent component) {
		return component.getTooltip() != null && !"".equals(component.getTooltip());
	}

	protected String createOuterTooltipStyle() {
		final StringBuilder tooltipStyle = new StringBuilder();
		tooltipStyle.append("display: none;");
		tooltipStyle.append("position: absolute;");
		tooltipStyle.append("background-color: white;");
		tooltipStyle.append("border-style: solid;");
		tooltipStyle.append("border-width: 1px;");
		tooltipStyle.append("width: 200px;");
		tooltipStyle.append("z-index: 200;");
		tooltipStyle.append("min-height: 25px;");
		return tooltipStyle.toString();
	}
}
