/**
 * jQuery-Plugin "butterHandleAutoComplete" for text autocomplete tag. Initializes auto complete functionality to
 * text component.
 *
 * How to use:
 * jQuery("#selector").butterHandleAutoComplete();
 */
(function ($) {
    // extend jQuery --------------------------------------------------------------------

    $.fn._butterHandleAutoComplete = function () {
        return this.each(function () {
            var $originalElement = $(this);
            var $input = $originalElement.prev();

            $input.attr('autocomplete', 'off');
            $input.attr('autocorrect', 'off');

            $originalElement.find('li').on("click", function () {
                $input.val($(this).attr("data-select-value")).change();
            })

        });
    };

}(jQuery));