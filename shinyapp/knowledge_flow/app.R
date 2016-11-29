#
# This is a Shiny web application of the knowledge flow model.
#

library(shiny)
source('knowledge_flow.R')

ui <- shinyUI(bootstrapPage(
  
  tags$head(tags$script('

    window.addEventListener("message", function (e) {
      var el = $(document).find(".run"); 
      //el[0].mydata = JSON.parse(e.data);
      var msg = JSON.parse(e.data);
      if (!el[0].mydata) el[0].mydata = msg;
      if (msg.type) el[0].mydata.type = msg.type;
      if (msg.data) el[0].mydata.data = msg.data;
      if (msg.param) el[0].mydata.param = msg.param;
    });

    $(document).on("click", "button.run", function(evt) {
      var el = $(evt.target);
      el.trigger("change");
    });
    
    var runBinding = new Shiny.InputBinding();
    $(document).extend(runBinding, {
      find: function(scope) {
        return $(scope).find(".run");
      },
      getId: function(el) {
        return el.id;
      },
      getValue: function(el) {
        return el.mydata;
      },
      setValue: function(el, value) {
        //params = value;
      },
      subscribe: function(el, callback) {
        $(el).on("change.runBinding", function(e) {
          callback();
        });
      },
      unsubscribe: function(el) {
        $el.off(".runBinding");
      }
    });

    Shiny.inputBindings.register(runBinding);
  ')),
  
  tags$button(id="analysis", class="run btn btn-default", type="button", 'Analyze'),
  
  tags$p(),
  # verbatimTextOutput("input_string"),

  plotOutput(outputId = "main_plot")
  
  ))

#Define server logic required to draw a histogram
server <- shinyServer(function(input, output) {
  
  # output$input_string = renderPrint({
  #   input$analysis
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

  observe({
    # Run whenever analyze button is pressed
    input$analysis

    output$main_plot = renderPlot({
      if (!is.null(input$analysis)) {
        # 1. Event network-based complexity
        if (input$analysis$type == "str.comp") {
          plot.str.complexity(input$analysis$data$event_seq, input$analysis$data$plot_seq)
        }
        # 2. Knowledge flow
        else if (input$analysis$type == "kf" && !is.null(input$analysis$param)){
          # story
          story <- list(title="TBA",
                        no.agents=length(input$analysis$data$agents),
                        agents=simplify2array(input$analysis$data$agents),
                        no.events=input$analysis$data$no.events,
                        events=seq_len(input$analysis$data$no.events),
                        plot.seq=simplify2array(input$analysis$data$plot_seq), # event ids in discourse order
                        event.seq=simplify2array(input$analysis$data$event_seq))
          # knowledge structure
          no.info <- length(input$analysis$data$info)
          no.know <- length(input$analysis$data$knowledge)
          knowledge.structure <- list(story=story,
                                      no.info=length(input$analysis$data$info),
                                      information=simplify2array(input$analysis$data$info),
                                      no.knowledge=no.know,
                                      knowledge=simplify2array(input$analysis$data$knowledge),
                                      percept=array(0, dim=c(story$no.events, story$no.agents, no.info)), #dimnames=c('event', 'agent', 'info')),
                                      impact=array(0, dim=c(no.info,no.know)), #dimnames=c('info', 'knowledge')),
                                      info.rule=NULL,
                                      initial.state=array(0, dim=c(story$no.agents,no.know)),
                                      initial.state.by.reader=array(0, dim=c(story$no.agents,no.know)))
          # Perception links
          ptable <- simplify2array(input$analysis$data$perception)
          for (i in c(1:dim(ptable)[1])) {
            row <- ptable[i,]
            knowledge.structure$percept[row$event,row$agent,row$info] <- row$percept
          }
          # Impact links
          itable <- simplify2array(input$analysis$data$impact)
          for (i in c(1:dim(itable)[1])) {
            row <- itable[i,]
            knowledge.structure$impact[row$info, row$know] <- row$impact
          }
          
          order <- input$analysis$param$order
          kf <- knowledge.flow(knowledge.structure, perspective.of.reader=FALSE, order=tolower(order), update.method=DST)
          kf.r <- knowledge.flow(knowledge.structure, perspective.of.reader=TRUE, order=tolower(order), update.method=DST)
          i.agent <- as.integer(input$analysis$param$agent / 2) + 1
          if (i.agent > 1 && (input$analysis$param$agent %% 2) == 1) kf <- kf.r
          agent <- story$agents[i.agent]
          kn.list <- simplify2array(input$analysis$param$knlist)
          #plot.kf.all(kf, kf.r, story$agents, "")
          plot.agent.kf(kf, kn.list, agent, i.agent)
        }
        # 3. Entropy-based complexity
        else if (input$analysis$type == "ent.comp") {
          # story
          story <- list(title="TBA",
                        no.agents=length(input$analysis$data$agents),
                        agents=simplify2array(input$analysis$data$agents),
                        no.events=input$analysis$data$no.events,
                        events=seq_len(input$analysis$data$no.events),
                        plot.seq=simplify2array(input$analysis$data$plot_seq), # event ids in discourse order
                        event.seq=simplify2array(input$analysis$data$event_seq))
          # knowledge structure
          no.info <- length(input$analysis$data$info)
          no.know <- length(input$analysis$data$knowledge)
          knowledge.structure <- list(story=story,
                                      no.info=length(input$analysis$data$info),
                                      information=simplify2array(input$analysis$data$info),
                                      no.knowledge=no.know,
                                      knowledge=simplify2array(input$analysis$data$knowledge),
                                      percept=array(0, dim=c(story$no.events, story$no.agents, no.info)), #dimnames=c('event', 'agent', 'info')),
                                      impact=array(0, dim=c(no.info,no.know)), #dimnames=c('info', 'knowledge')),
                                      info.rule=NULL,
                                      initial.state=array(0, dim=c(story$no.agents,no.know)),
                                      initial.state.by.reader=array(0, dim=c(story$no.agents,no.know)))
          # Perception links
          ptable <- simplify2array(input$analysis$data$perception)
          for (i in c(1:dim(ptable)[1])) {
            row <- ptable[i,]
            knowledge.structure$percept[row$event,row$agent,row$info] <- row$percept
          }
          # Impact links
          itable <- simplify2array(input$analysis$data$impact)
          for (i in c(1:dim(itable)[1])) {
            row <- itable[i,]
            knowledge.structure$impact[row$info, row$know] <- row$impact
          }
          
          order <- "Discoure"
          kf <- knowledge.flow(knowledge.structure, perspective.of.reader=FALSE, order=tolower(order), update.method=DST)
          kf.r <- knowledge.flow(knowledge.structure, perspective.of.reader=TRUE, order=tolower(order), update.method=DST)
          
          # Parameters
          k.desired <- simplify2array(input$analysis$data$know_type)
          i.reader <- match(input$analysis$param$reader, story$agents)
          r.agents <- simplify2array(input$analysis$param$agents)
          i.r.agents <- match(r.agents, story$agents)
          
          cp.entropy <- narrative.complexity(story$no.events, kf[i.reader,,], kf.r, i.r.agents, k.desired)
          
          plot.ent.complexity(cp.entropy, r.agents)
        }
        else {
          
        }
      }
    })
  })
})

# Run the application 
shinyApp(ui = ui, server = server)
