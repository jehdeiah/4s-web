############################################################
# A computational model of Knowledge Flow
# (v. 20160201)
#
# The main purpose of this R program is to:
#    (1) Evaluate knowledge states and flows of narrative agents
#        using one of the two methods: Bayesian, DST
#    (2) Plot Knowledge Flows
#
# Knowledge flow is a line glaph in which x-axis is event order
# and y-axis is KS as degree-of-belief.
# This program produces KF as 3-dimensional array index by
# agent, knowledge, and event order. Using this array,
# KF of an agent about certain knowledge is easily plotted.
#
# The knowledge structure is extended with Information Rules.
#
# In addition, an evaluation of narrative complexity is
# implemented based on information entropy and temporal relation among events.
#
# -written by H. Tae Kwon
############################################################

#===========================================================
# Data structure of Narrative Structure
#
# Evets are indexed by the temporal order in the discourse time.
# Agents and Knowledge are named data.
# Events and Information are represented by index.
#
# Data structure of Story -- agents and events, and event ordering
# story <- list(title="title",
#				no.agent=n,
#				agents=(vector of agents' names),
#				no.events=m,
#				events=c(1:m), # event IDs...
#       plot.seq=(sequence of event IDs in discourse order),
#				event.seq=(sequence of event IDs in story-time order))
#
# Data structure of Knowledge Structure -- story, information, knowledge,
#                                       -- perception and impact links, information patterns (rules)
# knowledge.structure <- list(
#				story=story defined above,
#				no.info=k,
#				infomation=c(1:m) index vector,
#				no.knowledge=l,
#				knowledge=(vector of knowledge names),
#				percept=array[event, agent(index), info],
#				impact=array[info, knowledge],
#				info.rule=list of list(type="CO" or "RS" or "RE", args=(info vector),
#                               initial.state=array[agent(index), knowledge], # including reader
#                               initial.state.by.reader=array[agent(index), knowledge]) # reader's assumption about characters

#===========================================================
# Handling Information Rules
#
# Information rule is a type of combination of information
# and each type has different effect towards the repect knowledge.
# In this version, four types are defined:
#    CO - combination
#    RE - recurrence
#    RS - reversal
#    NE - non-existence
#
# Data structure for monitoring rules
# rule.monitor <- list(
#	cursor=list(rule=rule, 
#				open=(open list of info), 
#				closed=(closed list of info),
#				fired=TRUE if all infos uncovered),
#	assert.CO=TRUE or FALSE,
#	reversal=(vector of infos to support reversely)
#	recurrent.knowledge=knowledge not to be supported in current visiting ; only care about single knowledge per each visit
#       assert.NE=TRUE or FALSE,
#)

#-----------------------------------------------------------
# Check information rules whether each of which is fired given new info
information.rule <- function(rule.monitor, info) {
    result.cursor <- NULL
    rule.monitor$assert.CO <- FALSE
    rule.monitor$reversal <- NULL
    rule.monitor$recurrent.knowledge <- NULL
    rule.monitor$assert.NE <- FALSE
    for (c in rule.monitor$cursor) {
        if (info %in% c$rule$args) {
            c$open <- c$open[c$open != info]
            c$closed <- append(c$closed, info)
            if (c$rule$type == "CO") {
                c$fired <- (length(c$open) == 0)
                rule.monitor$assert.CO <- !c$fired
            }
            else if (c$rule$type == "RS") {
                # assume binary relation
                c$fired <- (c$rule$args[1]==info && c$rule$args[2] %in% c$closed)
                if (c$fired) rule.monitor$reversal <- append(rule.monitor$reversal, c$rule$args[2])
            }
            else if (c$rule$type == "RE") {
                # assume single knowledge per one
                c$fired <- (c$rule$args[1] == info)
                if (c$fired) {
                    k.ignored <- NULL
                    if (c$rule$args[2] %in% c$closed) k.ignored <- c$rule$args[4]
                    else k.ignored <- c$rule$args[3]
                    rule.monitor$recurrent.knowledge <- append(rule.monitor$recurrent.knowledge, k.ignored)
                }
            }
            else if (c$rule$type == "NE") {
                # assume binary relation
                c$fired <- (c$rule$args[1]==info && !(c$rule$args[2] %in% c$closed))
                rule.monitor$assert.NE <- c$rule$args[1]==info && c$rule$args[2] %in% c$closed
            }
        }
        result.cursor <- append(result.cursor, list(c))
    }
    rule.monitor$cursor <- result.cursor
    rule.monitor
}

# Initialize rule monitor with given rules
init.rule.monitor <- function(rules) {
    monitor <- list(cursor=NULL, assert.CO=FALSE, reversal=NULL, recurrent.knowledge=NULL, assert.NE=FALSE)
    cursor <- NULL
    for (r in c(1:length(rules))) {
        c <- list(rule=rules[[r]], open=rules[[r]]$args, closed=NULL, fired=FALSE)
        cursor <- append(cursor, list(c))
    }
    monitor$cursor <- cursor
    monitor
}

#===========================================================
# Reasoning Model for Evaluating Knowledge State
#
# A reasoning model M is represented as a list consisting of 4 elements:
#    M.base is a model parameter that the model maintains internally.
#    M.init <- function(no.knowledge, pos, neg)
#       gives initial probabilities of truth and falseness of knowledge
#    M.update <- function(base, knowledge, support, fuzzy=TRUE)
#       updates a model parameter 'base' with 'support' to 'knowledge'.
#       If fuzzy is false, the probability of the supported hypothesis is amplified.
#       (This is reserved for future usage.)
#    M.state <- function(base, knowledge)
#       returns the degree-of-belief value in [-1,1] as knowledge state.

#-----------------------------------------------------------
# Naive Bayesian Method
#
# The model parameter is the probability distribution of the truth of hypothesis.
Bayesian.init <- function(no.knowledge, pos=NULL, neg=NULL) {
    prior <- array(0.5, dim=c(no.knowledge,2))
    for (i in 1:no.knowledge) {
        if (!is.null(pos)) prior[i,1] <- pos[i]
        if (!is.null(neg)) prior[i,2] <- neg[i]
        if (sum(prior[i,]) != 0) prior[i,] <- prior[i,]/sum(prior[i,])
    }
    prior
}

# Update function uses a sort of Bayes' rule.
# In this function, 'support' is the same with effective impact value in papers.
# Once a hypothesis gets the probability 0 or 1, it is not affected by any further infomation.
# Thus the assigned value varies in [e, 1-e] with a small value e.
Bayesian.update <- function(base, knowledge, support, fuzzy=TRUE) {
    epsilon <- 0.01
    # P({positive, negative})
    p.i <- c((1 + support)/2, (1 - support)/2)
    if (!fuzzy) {
        # amplifies the effect to the supported hypothesis only.
        if (support > 0) p.i[2] <- epsilon*0.5 else p.i[1] <- epsilon*0.5
        p.i <- p.i/sum(p.i)
    }
    p.k <- p.i*base[knowledge,]
    p.k <- p.k/sum(p.k)
    if (p.k[1] == 0) p.k <- c(epsilon, 1-epsilon)
    else if (p.k[2] == 0) p.k <- c(1-epsilon, epsilon)
    base[knowledge,] <- p.k
    base
}

# KS is obtained by simple linear mapping.
Bayesian.state <- function(base, knowledge) {
    2*(base[knowledge,1] - 0.5)
}

# Global instance of naive Bayesian model
Bayesian <- list(base=NULL, init=Bayesian.init, update=Bayesian.update, state=Bayesian.state)

#-----------------------------------------------------------
# DST Method
#
# This method is based on Dempster-Shafer Theory to handle
# the information support to two opposite hypotheses independently.

# Initial mass setting. DoB 0 is the mass of (0,0,0,1)
DST.init <- function(no.knowledge, pos=NULL, neg=NULL) {
    mass <- array(0, dim=c(no.knowledge,4))
    for (i in 1:no.knowledge) {
        m <- c(0,0,0,0)
        if (!is.null(pos)) m[2] <- pos[i]
        if (!is.null(neg)) m[3] <- neg[i]
        if (sum(m) > 1) m <- m/sum(m)
        m[4] <- 1-sum(m)
        mass[i,] <- m
    }
    mass
}

# Updating mass uses the Dempster's rule of combination.
# To avoid the definite belief, mass is assigned by values in [e,1-e].
DST.update <- function(base, knowledge, support, fuzzy=TRUE) {
    # mass = {null, positive, negative, confused}
    mass <- base[knowledge,]
    m <- rep(0,times=4)
    epsilon <- 0.0001
    support <- if (support == 1) support-epsilon else if (support == -1) support+epsilon else support
    remain <- 1 - abs(support)
    i.opposite <- 3
    if (support > 0) m[2] <- support
    else if (support < 0) { m[3] <- abs(support); i.opposite <- 2 }
    if (fuzzy) {
        # share the remaining probability mass with confused hypothesis only.
        e <- min(epsilon, remain*0.5)
        m[i.opposite] <- e
        m[4] <- remain-e
    }
    else {
        # This amplifies the support to the hypothesis...
        e <- min(epsilon, abs(support))
        m[i.opposite] <- e
        m[4] <- e
        m <- m/sum(m)
    }
    min.i <- match(min(m),m)
    if (m[min.i] < epsilon) {
        rest <- c(1:4)[c(1:4) != min.i]
        m[rest] <- m[rest]/(sum(m[rest])+epsilon)
        m[min.i] <- epsilon
    }
    # Dempster's rule
    K <- (mass[2:3] %*% m[3:2])
    s <- function(j) { mass[j]*sum(m[c(j,4)]) + mass[4]*m[j] }
    m.new <- c(0, s(2), s(3), mass[4]*m[4])/(1-K)
    base[knowledge,] = m.new/sum(m.new)
    base
}

# KS is the difference between bel(TRUE) and bel(FALSE)
DST.state <- function(base, knowledge) {
    mass <- base[knowledge,]
    # belief
    b <- c(0, mass[2], mass[3], sum(mass))
    state <- b[2] - b[3]
}

# Global instance of DST reasoning model
DST <- list(base=NULL, init=DST.init, update=DST.update, state=DST.state)

#===========================================================
# Evaluation of Knowledge Flow
# Knowledge flows are represented as 3-dimensional array indexed by agent, knowledge and event order

#-----------------------------------------------------------
# auxiliary functions

# List of indices that corresponds to elements of 'target' subset of 'set'
target.index <- function(set, target) {
    tlist <- target
    if (is.character(target) && target=="all") tlist <- set
    seq <- c(1:length(set))
    index <- NULL
    for (i in tlist) index <- append(index, seq[set==i])
    index
}

#-----------------------------------------------------------
# Calculate knowledge flows of agents
#  @IN persepctive.of.reader: TRUE or FALSE
#  @IN order: "discourse" or "story". The result of knowledge flows is along with this order.
knowledge.flow <- function(knowledge.structure, agent="all",
                           knowledge="all",
                           perspective.of.reader=FALSE,
                           order="discourse",
                           update.method=DST)
{
    story <- knowledge.structure$story
    agent.list <- target.index(story$agent, agent)
    kn.list <- target.index(knowledge.structure$knowledge, knowledge)
    kn.seq <- c(1:knowledge.structure$no.knowledge)
    kn.flow <- array(NA, dim=c(length(agent.list),length(kn.list),story$no.events))
    for (i.agent in agent.list) {
        event.seq <- story$plot.seq # discourse order by default, changed below when story order is requested.
    	rule.monitor <- init.rule.monitor(knowledge.structure$info.rule)
    	flow <- array(0, dim=c(length(kn.list),length(event.seq)))
    	kn.state <- knowledge.structure$initial.state[i.agent,] #array(0, dim=c(length(kn.list)))
        if (perspective.of.reader) kn.state <- knowledge.structure$initial.state.by.reader[i.agent,]
        update.method$base = update.method$init(length(kn.list))
        # In the reader's perspective, all agents including the reader itself acquire information along with discourse time.
        # In characters' perspective, characters accumulate information along with story time.
        along.story.order = !perspective.of.reader && story$agent[i.agent]!='Reader'
        if (along.story.order) event.seq <- story$event.seq
    	for (e in event.seq) {
            info.list <- knowledge.structure$information[knowledge.structure$percept[e,i.agent,] > 0]
            for (i.info in info.list) {
                rule.monitor <- information.rule(rule.monitor, i.info)
                # reverse the effects of misunderstood info
                for (m in rule.monitor$reversal) {
                    kn.affected <- kn.seq[knowledge.structure$impact[m,] != 0]
                    kn.affected <- kn.affected[kn.affected %in% kn.list]
                    for (k in kn.affected) {
                        match.k <- match(k, kn.list)
                        # assume the percept value to be 1
                        support <- 1*knowledge.structure$impact[m,k]
                        #kn.state[match.k] <- update.state(kn.state[match.k], -support)
                        update.method$base <- update.method$update(update.method$base, match.k, -support, FALSE)
                        kn.state[match.k] <- update.method$state(update.method$base, match.k)
                    }
                }
                if (!rule.monitor$assert.CO && !rule.monitor$assert.NE) {
                    kn.affected <- c(1:knowledge.structure$no.knowledge)[knowledge.structure$impact[i.info,] != 0]
                    # filtering by recurrent knowledge
                    kn.affected <- kn.affected[!kn.affected %in% rule.monitor$recurrent.knowledge]
                    kn.affected <- kn.affected[kn.affected %in% kn.list]
                    for (k in kn.affected) {
                        match.k <- match(k, kn.list)
                        support <- knowledge.structure$percept[e,i.agent,i.info]*knowledge.structure$impact[i.info,k]
                        #kn.state[match.k] <- update.state(kn.state[match.k], support)
                        update.method$base <- update.method$update(update.method$base, match.k, support)
                        kn.state[match.k] <- update.method$state(update.method$base, match.k)
                    }
                }
            }
            e.index <- match(e,story$plot.seq)
            if (order=="story") e.index <- match(e,story$event.seq)
            flow[,e.index] <- kn.state
        }
        kn.flow[match(i.agent,agent.list),,] <- flow#[,event.order]
    }
    kn.flow
}

#===========================================================
# Evaluation of Narrative Complexity using Entropy
#
# The narrative complexity is the amount of information processed by readers.
# Assuming knowledge flows as the message to be processed,
# the complexity is calculated using information gain in KFs.

#-----------------------------------------------------------
# Computation of entropy

# Kullback-Leibler divergence Q from P
KL.divergence <- function(p,q) {
    if (p == q) { 0; return }
    epsilon <- 0.0001
    d <- c(0,0)
    if (p == 0) d[1] <- 0
    else {
        if (q == 0) q <- epsilon
        d[1] <- p*(log(p,base=2) - log(q,base=2))
    }
    p.r <- 1 - p
    q.r <- 1 - q
    if (p.r == 0) d[2] <- 0
    else {
        if (q.r == 0) q.r <- epsilon
        d[2] <- p.r*(log(p.r,base=2) - log(q.r,base=2))
    }
    sum(d)
}

# Information gain throughout a series of believes from the 'current' belief
entropy <- function(dist) {
    n <- length(dist)
    if (n == 0) { 0; return }
    i <- array(0, n)
    if (n == 1) {
        p = dist[1];
        i[1] = -p*log(p,base=2) - (1-p)*log(1-p,base=2)
    }
    for (k in c(2:n)) {
        i[k] <- i[k-1] + KL.divergence(dist[k], dist[k-1])
    }
    i
}

# Relative entropy of 'dist2' w.r.t dist1
cross.entropy <- function(dist1, dist2) {
    err <- 0.05
    n = length(dist1)
    if (n != length(dist2)) { NULL; return }
    c <- array(0, n)
    c[1] <- KL.divergence(dist1[1], dist2[1])
    for (k in c(2:n)) {
        if (abs(dist1[k]-dist1[k-1])>err || abs(dist2[k]-dist2[k-1])>err) {
            c[k] <- c[k-1] + KL.divergence(dist1[k],dist2[k])
        }
        else {
            c[k] <- c[k-1]
        }
    }
    c
}

#-----------------------------------------------------------
# Complexity evaluation

narrative.complexity <- function(no.events,
                                 kf.of.reader, # KF of reader, 2-D array
                                 kf.r, # KF of all, 3-D array
                                 r.agents, # index array of counted character
                                 k.desired # desired states of each knowledge
                                 ) {
    m <- length(r.agents) + 2
    n <- m + 2
    cp.entropy <- array(0,c(n,no.events))
    for (k in c(1:length(k.desired))) {
        cp.entropy[1,] <- cp.entropy[1,] + entropy(kf.of.reader[k,]/2+0.5)
        cp.entropy[m,] <- cp.entropy[1,]
        for (a in c(2:(m-1))) {
            cp.entropy[a,] <- cp.entropy[a,] + cross.entropy(kf.of.reader[k,]/2+0.5,kf.r[r.agents[a-1],k,]/2+0.5)
            cp.entropy[m,] <- cp.entropy[m,] + cp.entropy[a,]
        }
        for (e in c(1:no.events)) {
            #cp.entropy.1[5,e] <- c.entropy(kf[i.reader,k,e]/2+0.5,k.desired[k])
            cp.entropy[m+1,e] <- cp.entropy[m+1,e] + KL.divergence(k.desired[k],kf.of.reader[k,e]/2+0.5)
        }
        cp.entropy[m+2,1] <- cp.entropy[m,1]
        for (i in c(2:no.events)) {
            cp.entropy[m+2,i] <- cp.entropy[m,i] - cp.entropy[m,i-1]
        }
    }
    cp.entropy
}

#===========================================================
# Evaluation of Narrative Complexity using Event-network
#
# The narrative complexity is the amount of information processed by readers.
# Assuming temporally twisted events are about to be reinterpreted,
# the complexity is calculated by counting events not in order.
#
# At this point, only the temporal relationship is is in account.
# (i.e, all the events are tied in a single causal chain.)

structural.complexity <- function(plot.seq, # sequence of event IDs in discourse order
                                  event.seq # sequence of event IDs in story-time order
                                  ) {
    # index sequence
    n <- length(plot.seq)
    #discourse.seq <- c(1:n)
    story.seq <- match(plot.seq, event.seq)
    # result: 1 - past hidden, 2 - future shown, 3 - complexity 1, 4 - complexity 2 (acc.)
    str.complexity <- array(0, c(4, n))
    # 1 - Preceding-events omitted (past hidden)
    max.eid <- 0
    for (i in c(1:n)) {
        if (max.eid < story.seq[i]) max.eid <- story.seq[i]
        str.complexity[1,i] <- max.eid - i
    }
    # 2 - Post-events presented (future shown)
    for (i in c(2:n)) {
        prev.events <- story.seq[1:i-1]
        str.complexity[2,i] <- length(prev.events[prev.events > story.seq[i]])
    }
    # 3 - complexity 1
    str.complexity[3,] <- str.complexity[1,] + str.complexity[2,]
    # 4 - complexity 2
    # calculates the sum of differential increments
    for (i in c(2:n))
    {
        d <- str.complexity[2,i] - str.complexity[2,i-1]
        str.complexity[4,i] <- str.complexity[4,i-1]
        if (d > 0) str.complexity[4,i] <- str.complexity[4,i] + d
    }
    # return
    str.complexity
}

#==========================================================
# Detection of Suspense Situations

fuzzy.set <- list(T = function(y) ifelse(y > 0 && y <= 1, y, 0),
                  N = function(y) ifelse(y > -0.5 && y < 0.5, 1 - 2 * abs(y), 0), 
#                  N = function(y) ifelse(y > -0.5 && y < 0.5, 1 - 2 * 0.8 * abs(y), 
#                                         ifelse(abs(y) < 0.9, 0.45 - 0.5 * abs(y), 0)),
                  F = function(y) ifelse(y >= -1 && y < 0, -y, 0))

membership <- function(x) {
    mapply(function(f, n) c(n, f(x)), fuzzy.set, names(fuzzy.set))
}

fuzzy.relation <- function(x, y) {
    class <- names(fuzzy.set)
    r <- Reduce(rbind, apply(expand.grid(class, class), MARGIN = 1,
                      FUN = function(p) {
                          p <- as.character(unlist(p))
                          data.frame(V1 = p[1], V2 = p[2],
                                     V3 = min((fuzzy.set[[p[1]]])(x),
                                     (fuzzy.set[[p[2]]])(y)))}))
    r[order(-r[, 3]), ]
}

Type.A <- data.frame(V1 = c('T', 'T', 'F', 'F'),
                     V2 = c('F', 'N', 'N', 'T'))
Type.B <- data.frame(V1 = c('T', 'N', 'F'),
                     V2 = c('T', 'N', 'F'))
Sudden <- data.frame(V1 = c('T', 'F'),
                     V2 = c('F', 'T'))
Confirm <- data.frame(V1 = c('T', 'F'),
                      V2 = c('T', 'F'))

suspense.situation <- function(kf.r, kf.c, kf.c.r, alpha.A = 0.3, alpha.B = 0.2, alpha.C = 0.7) {
    if (is.null(dim(kf.r))) {
        kf.r <- matrix(kf.r, nrow = 1)
        kf.c <- matrix(kf.c, nrow = 1)
        kf.c.r <- matrix(kf.c.r, nrow = 1)
    }
    n.k <- dim(kf.r)[1]
    n.e <- dim(kf.r)[2]
    # ignores the leading zeros in kf
    Mask <- function(l, x) c(l, l[length(l)] && x)
    # gets fuzzy relations
    in.relation <- function(rset, ref) {
        if (nrow(rset) == 0) return(FALSE)
        sum(duplicated(rbind(ref, rset))[-seq_len(nrow(ref))]) > 0
    }
    # determine whether a pair of knowledge state is of the given type.
    is.ks.in.type <- function(ks1, ks2, type, alpha) {
      r <- fuzzy.relation(ks1, ks2)
      r.cut <- r[r[, 3] >= alpha, 1:2]
      in.relation(r.cut, type)
#         sum(mapply(function(x, y) {
#                 r <- fuzzy.relation(x, y)
#                 r.cut <- r[r[, 3] >= alpha, 1:2]
#                 in.relation(r.cut, type)
#             }, ks1, ks2)) 
    }
    # detects any given type per each events in discourse order
    is.kf.in.type <- function(kf1, kf2, type, alpha) {
      sapply(seq_len(n.e), function(l)
        is.ks.in.type(kf1[l], kf2[l], type, alpha))
      #       sapply(seq_len(n.e), function(l)
      #         any.is.in.type(kf1[, l], kf2[, l], type, alpha))
    }
    # detects Type A per each events in discourse order
    is.in.type.A <- function(kf1, kf2, alpha) {
      rowSums(mapply(function(r) {
        r.in.type.A <- is.kf.in.type(kf1[r, ], kf2[r, ], Type.A, alpha)
        leading.zero <- Reduce(Mask, kf1[r, ] == 0)
        r.in.type.A[leading.zero] <- 0
        r.in.type.A
      }, seq_len(nrow(kf1))))
        #is.in.type(kf, kf2, type.A, alpha)
    }
    # detects Type A per each events in discourse order
    # (leading zeros are removed.)
    is.in.type.B <- function(kf1, kf2, alpha) {
        rowSums(mapply(function(r) {
          r.in.type.B <- is.kf.in.type(kf1[r, ], kf2[r, ], Type.B, alpha) #& (kf1[r, ] * kf2[r, ] >= 0)
          leading.zero <- Reduce(Mask, kf1[r, ] == 0)
          r.in.type.B[leading.zero] <- 0
          r.in.type.B
        }, seq_len(nrow(kf1))))
    }
    # detects Type A per each events in discourse order
    # (leading zeros are removed.)
    is.in.type.C <- function(kf1, kf2, kf3, alpha) {
      # Args:
      #   kf1, kf2, kf3: typically KF_R, KF_C, KF_C|R, repectively.
      rowSums(mapply(function(r) {
        realize.C <- c(FALSE, sapply(seq_len(n.e)[-1], function(i) {
          is.ks.in.type(kf1[r, i], kf1[r, i - 1], Type.A, alpha) &&
          #is.ks.in.type(kf2[r, i], kf2[r, i - 1], Confirm, alpha) &&
          is.ks.in.type(kf1[r, i], kf2[r, i], Confirm, alpha) &&
          is.ks.in.type(kf3[r, i], kf3[r, i - 1], Type.A, alpha) &&
          is.ks.in.type(kf3[r, i], kf2[r, i], Confirm, alpha.C)
        }))
        r.in.type.C <- realize.C
        end.C <- seq_along(realize.C)[realize.C]
        if (length(end.C) > 0) {
          detect.C <- function(begin, end) {
            if (begin < end) {
              for (i in seq(end - 1, begin, -1)) {
                if (!is.ks.in.type(kf2[r, i], kf2[r, end], Confirm, alpha) ||
                    !is.ks.in.type(kf2[r, i], kf1[r, i], Type.A, alpha)) break
                r.in.type.C[i] <<- TRUE
              }
            }
            #if (sum(in.type.C[begin:end]) == 1) in.type.C[end] <<- FALSE
          }
          mapply(detect.C, c(1, (end.C + 1)[-length(end.C)]), end.C)
        }
        leading.zero <- Reduce(Mask, kf3[r, ] == 0)
        r.in.type.C[leading.zero] <- 0
        r.in.type.C
      }, seq_len(nrow(kf1))))
    }
    in.type.A <- is.in.type.A(kf.r, kf.c.r, alpha.A)
    in.type.B <- is.in.type.B(kf.r, kf.c.r, alpha.B)
    in.type.C <- is.in.type.C(kf.r, kf.c, kf.c.r, alpha.C)
    return(list(A = in.type.A , B = in.type.B , C = in.type.C))
#     leading.zero <- Reduce(Mask, colSums(kf.r != 0) == 0)
#     in.type.B[leading.zero] <- 0
#     realize.C <- c(FALSE, sapply(seq_len(n.e)[-1], function(i) {
#         past.story.order <- match(story$plot.seq[1:i], story$event.seq)
#         recent <- past.story.order[past.story.order < past.story.order[i]]
#         if (length(recent) == 0) return(FALSE)
#         recent.plot.order <- story$plot.seq[match(max(recent), story$event.seq)]
#         any.is.in.type(kf.r[, i], kf.r[, i - 1], Type.A, alpha.C) &&
#         any.is.in.type(kf.c[, i], kf.c[, i - 1], Confirm, alpha.C) &&
#         any.is.in.type(kf.r[, i], kf.c[, i], Confirm, alpha.C) &&
#         any.is.in.type(kf.c.r[, i], kf.c.r[, i - 1], Type.A, alpha.C) &&
#         any.is.in.type(kf.c.r[, i], kf.c[, i], Confirm, alpha.C)
#     }))
#     in.type.C <- realize.C
#     end.C <- seq_len(n.e)[realize.C]
#     if (length(end.C) > 0) {
#       # to story time
#       story.end.C <- match(end.C, story$event.seq)
#       
#         detect.C <- function(begin, end) {
#             if (begin < end) {
#                 for (i in seq(end - 1, begin, -1)) {
#                     if (!any.is.in.type(kf.c[, i], kf.c[, end],
#                                         Confirm, alpha.C) ||
#                         !any.is.in.type(kf.c[, i], kf.r[, i],
#                                         Type.A, alpha.C)) break
#                     in.type.C[i] <<- TRUE
#                 }
#             }
#             #if (sum(in.type.C[begin:end]) == 1) in.type.C[end] <<- FALSE
#         }
#         mapply(detect.C, c(1, (end.C + 1)[-length(end.C)]), end.C)
#     }
#     leading.zero.C <- Reduce(Mask, colSums(kf.c.r != 0) == 0)
#     in.type.C[leading.zero.C] <- FALSE
#     list(A = in.type.A , B = in.type.B , C = in.type.C)
}

#-----------------------------------------------------------
# Example: plotting KF of single agent
plot.agent.kf <- function(kf, agent.name, agent.index) {
    kn = dim(kf)[2]
    col = c(1:kn)
    layout(mat=matrix(c(1,2), nrow=2, ncol=1), heights=c(0.9,0.1))
    ts.plot(t(kf[agent.index,,]), col=col,
            xlab="Event order", ylab="Knowledge state",
            main=paste("Knowledge flow of", agent.name))
    par(mar=c(0,0,0,0),mgp=c(0,0,0))
    plot.new()
    legend("top",paste0("K",c(1:kn)), col=col, lty=1, box.lwd=0, horiz=TRUE)
}

# Example: plotting all KFs collectively
# [1st row]  ... KF of agents
# [2nd row]  ... KF of agents judged by reader |R
# [3rd row]  legend: 'note' -- K1 ... -- Kn
plot.kf.all <- function(kf, kf.r, agent.names, note) {
  no.agents <- dim(kf)[1]
  no.knowledge <- dim(kf)[2]
  no.events <- dim(kf)[3]
  layout(mat = matrix(c(seq(2*no.agents), rep(2*no.agents+1, no.agents)), nrow = 3, byrow = T), heights = c(0.46,0.46,0.08))
  
  a <- utf8ToInt('a')
  index <- paste0("(", sapply(a:(a + no.agents), intToUtf8), ") ", agent.names)
  index.r <- paste0(index, "|R")
  col <- 2:(no.knowledge + 1)
  lty <- 1:no.knowledge
  
  def.par <- par()
  # all KFs
  sapply(1:no.agents, function(a) {
    par(mar = c(5,2.5,0,2), mgp=c(1.5,0.5,0))
    ts.plot(t(kf[a, 1:no.knowledge,]), col=col, lty=lty, xlab="Event order", ylab="Knowledge state", ylim=c(-1,1))
    title(sub=index[a], mgp=c(2.5,0,0), cex.sub=1.3)
  })
  # all KFs w.r.t reader
  sapply(1:no.agents, function(a) {
    par(mar=c(5,2.5,0,2), mgp=c(1.5,0.5,0))
    ts.plot(t(kf.r[a, 1:no.knowledge,]), col=col, lty=lty, xlab="Event order", ylab="Knowledge state", ylim=c(-1,1))
    title(sub=index.r[a], mgp=c(2.5,0,0), cex.sub=1.3)
  })
  par(mar=c(0,0,0,0), mgp=c(0,0,0))
  plot.new()
  legend("right", c(note, paste0("K", 1:no.knowledge)),col=1:(no.knowledge+1),lty=0:no.knowledge, bty='n', horiz=TRUE)#, cex=0.9)
  
  #par(def.par)
}

# Example: plotting structural complexity with two event sequences
plot.str.complexity <- function(event.seq, plot.seq) {
  
  result <- structural.complexity(plot.seq, event.seq)
  
  col <- c(3,8,2,4)
  lty <- c(2,3,1,1)
  lwd <- c(1.5,1.5,1.0,1.0)
  label <- c("Preceding-events omitted", "Post-events presented", "Complexity 1:\nSum of out-of-order events", "Complexity 2:\nReinterpreted events \n(accumulated)")
  ln <- c(1:4)
  layout(mat=matrix(c(1,2), nrow=1, ncol=2), widths=c(0.7,0.3))
  par(mar = c(3,2.5,2,0.6), mgp=c(1.5,0.5,0))
  ts.plot(t(result), col=col, lwd=lwd, lty=lty, xlab="Discoure order", ylab="# Events", ylim=c(0,ceiling(max(result)/5)*5))
  title(main="Structural Complexity")
  par(mar=c(0,0,0,0),mgp=c(0,0,0))
  plot.new()
  legend("right", label,
         col=col[ln],lty=lty[ln], bty='n', horiz=FALSE, cex=0.8)
}

#===========================================================
# This is the end of general KF code.

#-----------------------------------------------------------
# Outline of application
#
# 1) Initialize story and knowledge structure with files.
# 2) Initialize information rules and knowledge states, if needed.
# 3) Evaluate KFs and/or complexity.
# 4) Plot KFs and/or complexity graphs.

