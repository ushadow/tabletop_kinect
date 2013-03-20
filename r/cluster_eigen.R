library(mclust)
library(plyr)

ClusterEigen <- function(data, num.features) {
  lapply(num.features, Cluster1, data = data)
}

Cluster1 <- function(data, num.features) {
  mclust <- Mclust(data[, 1 : num.features], 1 : 20)
}

PlotClusterRes <- function(num.features, clusters) {
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
