package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

const val TAG = "EstoqueApp"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                EstoqueApp()
            }
        }
    }
}

data class Produto(
    val id: Int,
    val nome: String,
    val preco: Double,
    val quantidade: Int
)

/**
 * Paleta simples para botões (substitui as referências não resolvidas).
 */
@Composable
fun InfoButtonColors(): ButtonColors =
    ButtonDefaults.buttonColors(
        containerColor = Color(0xFF2196F3),
        contentColor = Color.White
    )

@Composable
fun WarningButtonColors(): ButtonColors =
    ButtonDefaults.buttonColors(
        containerColor = Color(0xFFFFC107),
        contentColor = Color.Black
    )

@Composable
fun DebugButtonColors(): ButtonColors =
    ButtonDefaults.buttonColors(
        containerColor = Color(0xFF9E9E9E),
        contentColor = Color.White
    )

@Composable
fun ErrorButtonColors(): ButtonColors =
    ButtonDefaults.buttonColors(
        containerColor = Color(0xFFF44336),
        contentColor = Color.White
    )

// “red” que você usava no botão Cadastrar
@Composable
fun RedButtonColors(): ButtonColors =
    ButtonDefaults.buttonColors(
        containerColor = Color(0xFF4CAF50), // verde principal do app para "Cadastrar"
        contentColor = Color.White
    )

@Composable
private fun EstoqueApp() {
    // Estados principais
    var produtos by remember { mutableStateOf(listOf<Produto>()) }
    var seqId by remember { mutableStateOf(1) }

    // Estados do formulário
    var nome by remember { mutableStateOf("") }
    var precoTexto by remember { mutableStateOf("") }
    var quantidadeTexto by remember { mutableStateOf("") }

    // Busca
    var filtro by remember { mutableStateOf("") }

    fun logSnapshot(reason: String) {
        Log.i(TAG, "Snapshot ($reason): qtdItens=${produtos.size}")
        produtos.forEach {
            Log.d(
                TAG,
                "Item: id=${it.id}, nome='${it.nome}', qtd=${it.quantidade}, preco=${"%.2f".format(it.preco)}"
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF4CAF50)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Titulo("GERENCIAMENTO DE ESTOQUE")

            // Formulário de cadastro
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Novo produto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = nome,
                            onValueChange = { nome = it },
                            label = { Text("Nome") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = precoTexto,
                            onValueChange = { precoTexto = it },
                            label = { Text("Preço") },
                            modifier = Modifier.width(140.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = quantidadeTexto,
                            onValueChange = { quantidadeTexto = it },
                            label = { Text("Qtd") },
                            modifier = Modifier.width(100.dp),
                            singleLine = true
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ActionButton(
                            text = "Cadastrar",
                            buttonColors = RedButtonColors(),
                            modifier = Modifier.weight(1f)
                        ) {
                            val preco = precoTexto.toDoubleOrNull()
                            val qtd = quantidadeTexto.toIntOrNull()
                            when {
                                nome.isBlank() -> {
                                    Log.w(TAG, "Cadastro: nome vazio")
                                }

                                preco == null || preco < 0.0 -> {
                                    Log.w(TAG, "Cadastro: preço inválido='$precoTexto'")
                                }

                                qtd == null || qtd < 0 -> {
                                    Log.w(TAG, "Cadastro: quantidade inválida='$quantidadeTexto'")
                                }

                                else -> {
                                    val novo = Produto(seqId++, nome.trim(), preco, qtd)
                                    produtos = produtos + novo
                                    Log.i(
                                        TAG,
                                        "Cadastro: id=${novo.id}, nome='${novo.nome}', preco=${"%.2f".format(preco)}, qtd=$qtd"
                                    )
                                    logSnapshot("apos cadastro")
                                    // Limpa campos
                                    nome = ""
                                    precoTexto = ""
                                    quantidadeTexto = ""
                                }
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        ActionButton(
                            text = "Limpar",
                            buttonColors = WarningButtonColors(),
                            modifier = Modifier.weight(1f)
                        ) {
                            nome = ""
                            precoTexto = ""
                            quantidadeTexto = ""
                            Log.d(TAG, "Formulário limpo")
                        }
                    }
                }
            }

            // Barra de busca
            OutlinedTextField(
                value = filtro,
                onValueChange = {
                    filtro = it
                    Log.d(TAG, "Filtro alterado para '$filtro'")
                },
                label = { Text("Buscar por nome") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Lista de produtos filtrada
            val listaFiltrada = remember(produtos, filtro) {
                if (filtro.isBlank()) produtos
                else produtos.filter { it.nome.contains(filtro, ignoreCase = true) }
            }

            if (listaFiltrada.isEmpty()) {
                Text(
                    text = if (produtos.isEmpty()) "Nenhum produto cadastrado" else "Nenhum resultado para '$filtro'",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listaFiltrada, key = { it.id }) { p ->
                        ProdutoItem(
                            produto = p,
                            onEntrada = { delta ->
                                val novo = p.copy(quantidade = p.quantidade + delta)
                                produtos = produtos.map { if (it.id == p.id) novo else it }
                                Log.i(
                                    TAG,
                                    "Entrada: id=${p.id}, +$delta (de ${p.quantidade} para ${novo.quantidade})"
                                )
                                logSnapshot("apos entrada")
                            },
                            onSaida = { delta ->
                                val finalQtd = p.quantidade - delta
                                if (finalQtd < 0) {
                                    Log.e(
                                        TAG,
                                        "Saída inválida: id=${p.id}, tentativa=$delta, estoqueAtual=${p.quantidade}"
                                    )
                                } else {
                                    val novo = p.copy(quantidade = finalQtd)
                                    produtos = produtos.map { if (it.id == p.id) novo else it }
                                    Log.i(
                                        TAG,
                                        "Saída: id=${p.id}, -$delta (de ${p.quantidade} para ${novo.quantidade})"
                                    )
                                    if (finalQtd == 0) {
                                        Log.w(TAG, "Atenção: produto id=${p.id} zerou o estoque")
                                    }
                                    logSnapshot("apos saida")
                                }
                            },
                            onRemover = {
                                produtos = produtos.filterNot { it.id == p.id }
                                Log.w(TAG, "Removido: id=${p.id}, nome='${p.nome}'")
                                logSnapshot("apos remocao")
                            }
                        )
                    }
                }
            }

            // Rodapé com ações rápidas de log
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionButton(
                    text = "LOG I",
                    buttonColors = InfoButtonColors(),
                    modifier = Modifier.weight(1f)
                ) { Log.i(TAG, "Info: totalItens=${produtos.size}") }
                Spacer(Modifier.width(8.dp))
                ActionButton(
                    text = "LOG W",
                    buttonColors = WarningButtonColors(),
                    modifier = Modifier.weight(1f)
                ) { Log.w(TAG, "Warning: filtroAtual='$filtro'") }
                Spacer(Modifier.width(8.dp))
                ActionButton(
                    text = "LOG D",
                    buttonColors = DebugButtonColors(),
                    modifier = Modifier.weight(1f)
                ) { Log.d(TAG, "Debug: ids=${produtos.joinToString { it.id.toString() }}") }
            }
        }
    }
}

@Composable
private fun ProdutoItem(
    produto: Produto,
    onEntrada: (Int) -> Unit,
    onSaida: (Int) -> Unit,
    onRemover: () -> Unit
) {
    var ajusteTexto by remember { mutableStateOf("1") }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${produto.nome}  •  R$ ${"%.2f".format(produto.preco)}",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "ID: ${produto.id}   Estoque: ${produto.quantidade}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                ActionButton(
                    text = "Remover",
                    buttonColors = ErrorButtonColors(),
                    modifier = Modifier
                ) { onRemover() }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = ajusteTexto,
                    onValueChange = { ajusteTexto = it },
                    label = { Text("Qtd ajuste") },
                    modifier = Modifier.width(120.dp),
                    singleLine = true
                )
                ActionButton(
                    text = "Entrada",
                    buttonColors = InfoButtonColors(),
                    modifier = Modifier.weight(1f)
                ) {
                    val delta = ajusteTexto.toIntOrNull()
                    if (delta == null || delta <= 0) {
                        Log.w(TAG, "Entrada: quantidade inválida='$ajusteTexto' para id=${produto.id}")
                    } else {
                        onEntrada(delta)
                    }
                }
                ActionButton(
                    text = "Saída",
                    buttonColors = WarningButtonColors(),
                    modifier = Modifier.weight(1f)
                ) {
                    val delta = ajusteTexto.toIntOrNull()
                    if (delta == null || delta <= 0) {
                        Log.w(TAG, "Saída: quantidade inválida='$ajusteTexto' para id=${produto.id}")
                    } else {
                        onSaida(delta)
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    buttonColors: ButtonColors = ButtonDefaults.buttonColors(),
    modifier: Modifier = Modifier,
    block: () -> Unit
) {
    ElevatedButton(
        onClick = block,
        shape = RoundedCornerShape(5.dp),
        colors = buttonColors,
        modifier = modifier.height(44.dp)
    ) {
        Text(text = text)
    }
}

@Composable
fun Titulo(texto: String, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.secondary,
        modifier = modifier
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun EstoqueAppPreview() {
    MaterialTheme {
        EstoqueApp()
    }
}
