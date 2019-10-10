package ru.sbrf.factoring.assets

case class LogRow(author: String,
                  txTime: Long,
                  createdCount: Int = 0,
                  updatedCount: Int = 0)
