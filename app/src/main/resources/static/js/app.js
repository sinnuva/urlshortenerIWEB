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
                        //We get the "safe" attribute from the response and display a message accordingly
                        if (msg.properties.safe == true) {
                            $("#feedback").html(
                                "<div class='alert alert-info lead'><strong>URL is safe</strong></div>"
                            );
                        } else if (msg.properties.safe == false) {
                            $("#feedback").html(
                                "<div class='alert alert-danger lead'><strong>URL is NOT safe</strong></div>"
                            );
                        } else {
                            // Fallback if msg.safe is undefined
                            console.log("msg:safe: ",msg.safe);
                            $("#feedback").html(
                                "<div class='alert alert-warning lead'><strong>Unable to determine URL safety</strong></div>"
                            );
                        }
                        
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