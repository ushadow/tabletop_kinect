library(mclust)
library(plyr)

ClusterEigen <- function(data, num.features) {
  lapply(num.features, Cluster1, data = data)
}

Cluster1 <- function(data, num.features) {
  mclust <- Mclust(data[, 1 : num.features], 1 : 20)
}

# Plot loglik or BIC with different number of features chosen.
PlotFeatureComp <- function(num.features, clusters) {
  loglik <- sapply(clusters, function(x) { x$loglik });
  bic <- sapply(clusters, function(x) { x$bic });
  ylim <- c(min(bic), max(loglik))
  linecols <- c('red', 'blue') 
  pch <- 21 : 22
  plot(num.features, loglik, ylim = ylim, type = 'o', col = linecols[1],
       ylab = 'loglik / bic', pch = pch[1]);
  lines(num.features, bic, ylim = ylim, type = 'o', col = linecols[2],
        pch = pch[2]); 
  legend('bottomleft', legend = c('loglik', 'bic'), col = linecols, lty = 1,
         pch = pch)
}

PlotBicVsComponent <- function(num.components, cluster) {
  modelName <- cluster$modelName
  plot(num.components, cluster$BIC[, modelName], type = 'o',
       xlab = "number of clusters", ylab = 'BIC')
  title <- sprintf("Model name = %s", modelName) 
  title(title)
}

SaveClassification <- function(clusters, filename) {
  classes <- ldply(clusters, function(x) { x$classification })
  write.csv(classes, filename, row.names = FALSE)
}

PlotClassifcation <- function(cluster, ylab) {
  data <- eval.parent(cluster$call$data)
  classification <- cluster$classification
  ylim <- c(min(data), max(data)) 
  xlim <- c(min(classification), max(classification))
  plot(1, type = 'n', ylim = ylim, xlim = xlim, ylab = ylab, xlab = 'class')
  nclass <- cluster$G
  col <- palette()[1 : nclass]
  sapply(1 : nclass, PlotOneClass, classification = classification, data = data,
         col = col)
  title('classification')
}

PlotOneClass <- function(class.label, classification, data, col) {
  class.data <- data[classification == class.label]
  points(rep(class.label, length(class.data)), class.data, col =
         col[class.label])
}

PlotMcluster <- function(cluster, prefix) {
  what <- c('BIC', 'classification', 'uncertainty', 'density')
  sapply(what, PlotMcluster1, cluster = cluster, prefix = prefix)
}

PlotMcluster1 <- function(what, cluster, prefix) {
  png(paste(prefix, '-', what, '.png', sep = ''));
  plot(cluster, what = what)
  dev.off()
}

AveCov <- function(cov) {
  dim <- dim(cov)
  mean <- apply(cov, 3, function(x) { mean(diag(x)) })
  mean(mean)
}




