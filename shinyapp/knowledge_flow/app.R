#
# This is a Shiny web application of the knowledge flow model.
#

library(shiny)
source('knowledge_flow.R')

ui <- shinyUI(bootstrapPage(
  
  tags$head(tags$script('
                        var params = null;
                        window.addEventListener("message", function (e) {
                        var params = JSON.parse(e.data);
                        Shiny.onInputChange("param", params);
                        });
                        ')),
  
  #verbatimTextOutput("input_string"),
  
  plotOutput(outputId = "main_plot")
  
  ))

#Define server logic required to draw a histogram
server <- shinyServer(function(input, output) {
  
  # output$input_string = renderPrint({
  #   input$param
  # })
  
  # output$selectUI <- renderUI({
  #   input$param
  #   if (input$analyze > 0) {
  #   isolate({
  #   #if (!is.null(input$param) && !is.null(input$param$event_seq))
  #     param <- isolate(input$obj)
  #     items <- unlist(param$event_seq)
  #   selectInput("events","select...", items, multiple = T, selectize = F)
  #   })
  #   }
  # })
  
  output$main_plot = renderPlot({
    if (!is.null(input$param)) {
      if (input$param$type == "str.comp") {
        plot.str.complexity(input$param$data$event_seq, input$param$data$plot_seq)
      }
      else {
        
      }
    }
  })
})

# Run the application 
shinyApp(ui = ui, server = server)
