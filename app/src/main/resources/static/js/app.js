$(document).ready(
    /**
     * Initializes the document and sets up the form submission handler.
     */
    function () {
        $("#shortener").submit(
            /**
             * Handles the form submission event.
             * Prevents the default form submission and sends an AJAX POST request.
             * @param {Event} event - The form submission event.
             */
            function (event) {
                event.preventDefault();
                $.ajax({
                    type: "POST",
                    url: "/api/link",
                    data: $(this).serialize(),
                    /**
                     * Handles the successful AJAX response.
                     * Displays the shortened URL in the result div.
                     * @param {Object} msg - The response message.
                     * @param {string} status - The status of the response.
                     * @param {Object} request - The XMLHttpRequest object.
                     */
                    success: function (msg, status, request) {
                        $("#result").html(
                            "<div class='alert alert-success lead'><a target='_blank' href='"
                            + request.getResponseHeader('Location')
                            + "'>"
                            + request.getResponseHeader('Location')
                            + "</a></div>");
                    },
                    /**
                     * Handles the AJAX error response.
                     * Displays an error message in the result div.
                     */
                    error: function () {
                        $("#result").html(
                            "<div class='alert alert-danger lead'>ERROR</div>");
                    }
                });
            });
    });