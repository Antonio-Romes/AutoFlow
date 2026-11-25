import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/services/auth.service';

type WorkOrderStatus = 'Em andamento' | 'Aguardando peças' | 'Pronta' | 'Atrasada';
type StatusFilter = 'Todas' | WorkOrderStatus;

interface WorkOrder {
  id: string;
  customer: string;
  vehicle: string;
  service: string;
  status: WorkOrderStatus;
  dueDate: string;
  initials: string;
  color: string;
}

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="dashboard-shell">
      <aside class="sidebar">
        <a class="dashboard-brand" routerLink="/dashboard" aria-label="AutoFlow, início">
          <span class="brand-mark">A</span><span>autoflow</span>
        </a>
        <span class="nav-caption">MENU PRINCIPAL</span>
        <nav class="side-nav" aria-label="Menu principal">
          <a class="nav-link nav-link-active" routerLink="/dashboard"><span class="nav-icon">◫</span>Visão geral</a>
          <a class="nav-link" href="#ordens"><span class="nav-icon">▤</span>Ordens de serviço <span class="nav-count">12</span></a>
          <a class="nav-link" href="#clientes"><span class="nav-icon">♙</span>Clientes</a>
          <a class="nav-link" href="#agenda"><span class="nav-icon">▦</span>Agenda</a>
        </nav>
        <div class="sidebar-bottom">
          <div class="help-card"><span class="help-spark">✳</span><strong>Precisa de ajuda?</strong><span>Fale com nosso time de suporte.</span><a href="mailto:suporte@autoflow.local">Acessar suporte <span>↗</span></a></div>
          <button class="profile-button" type="button" (click)="logout()">
            <span class="profile-avatar">AF</span><span class="profile-copy"><strong>Administração</strong><small>Administrador</small></span><span class="profile-menu">⋯</span>
          </button>
        </div>
      </aside>

      <main class="dashboard-main">
        <header class="topbar">
          <div class="breadcrumbs"><span>Oficina</span><span class="crumb-divider">/</span><strong>Visão geral</strong></div>
          <div class="topbar-actions"><span class="today-label">Terça-feira, 25 de novembro</span><button class="icon-button notification-button" type="button" aria-label="Notificações">♧<i></i></button><span class="top-avatar">AF</span></div>
        </header>

        <div class="dashboard-content">
          <section class="welcome-row">
            <div><span class="eyebrow dashboard-eyebrow">TERÇA-FEIRA, 25 DE NOVEMBRO DE 2026</span><h1>Bom dia, equipe <span>✳</span></h1><p>Aqui está o resumo da sua oficina hoje.</p></div>
            <button class="primary-action" type="button" (click)="showNewOrderMessage()"><span>＋</span> Nova ordem de serviço</button>
          </section>
          @if (notice()) { <div class="dashboard-notice" role="status">{{ notice() }}</div> }

          <section class="metric-grid" aria-label="Resumo da oficina">
            <article class="metric-card"><div class="metric-heading"><span>Ordens abertas</span><span class="metric-icon icon-green">▤</span></div><div class="metric-value">{{ openOrders }}</div><div class="metric-foot"><span class="trend-up">↗ 12%</span><span> em relação ao mês passado</span></div><div class="metric-spark spark-green"><i></i><i></i><i></i><i></i><i></i><i></i><i></i><i></i><i></i><i></i><i></i><i></i><i></i></div></article>
            <article class="metric-card"><div class="metric-heading"><span>Em andamento</span><span class="metric-icon icon-blue">◷</span></div><div class="metric-value">{{ inProgressOrders }}</div><div class="metric-foot"><span class="metric-muted">Serviços na oficina agora</span></div><div class="metric-spark spark-blue"><i></i><i></i><i></i><i></i><i></i><i></i><i></i><i></i><i></i><i></i><i></i><i></i><i></i></div></article>
            <article class="metric-card"><div class="metric-heading"><span>Aguardando peças</span><span class="metric-icon icon-amber">⌁</span></div><div class="metric-value">{{ waitingOrders }}</div><div class="metric-foot"><span class="metric-muted">Acompanhe os fornecedores</span></div><div class="metric-spark spark-amber"><i></i><i></i><i></i><i></i><i></i><i></i><i></i><i></i><i></i><i></i><i></i><i></i><i></i></div></article>
            <article class="metric-card"><div class="metric-heading"><span>Faturamento do mês</span><span class="metric-icon icon-purple">↗</span></div><div class="metric-value metric-currency">R$ 24.850<span>,00</span></div><div class="metric-foot"><span class="trend-up">↗ 8,4%</span><span> em relação ao mês passado</span></div><div class="metric-spark spark-purple"><i></i><i></i><i></i><i></i><i></i><i></i><i></i><i></i><i></i><i></i><i></i><i></i><i></i></div></article>
          </section>

          <section class="dashboard-panels">
            <article class="panel orders-panel" id="ordens">
              <div class="panel-heading"><div><h2>Ordens de serviço</h2><p>Acompanhe os serviços recentes da oficina.</p></div><a class="text-action" href="#ordens">Ver todas <span>→</span></a></div>
              <div class="table-toolbar"><div class="status-filters" aria-label="Filtrar por status">
                @for (filter of filters; track filter) { <button type="button" class="filter-button" [class.filter-active]="activeFilter() === filter" (click)="activeFilter.set(filter)">{{ filter }}</button> }
              </div><label class="search-box"><span>⌕</span><input type="search" placeholder="Buscar ordem..." [value]="query()" (input)="updateQuery($event)" aria-label="Buscar ordem de serviço"></label></div>
              <div class="orders-table-wrap"><table><thead><tr><th>ORDEM</th><th>CLIENTE / VEÍCULO</th><th>SERVIÇO</th><th>STATUS</th><th>PREVISÃO</th><th></th></tr></thead><tbody>
                @for (order of filteredOrders(); track order.id) {
                  <tr><td><span class="order-number">{{ order.id }}</span></td><td><div class="customer-cell"><span class="customer-avatar" [style.background]="order.color">{{ order.initials }}</span><span><strong>{{ order.customer }}</strong><small>{{ order.vehicle }}</small></span></div></td><td class="service-cell">{{ order.service }}</td><td><span class="status-badge" [class]="statusClass(order.status)"><i></i>{{ order.status }}</span></td><td class="date-cell">{{ order.dueDate }}</td><td><button class="row-menu" type="button" [attr.aria-label]="'Mais opções para ' + order.id">···</button></td></tr>
                } @empty { <tr><td class="empty-state" colspan="6">Nenhuma ordem encontrada para este filtro.</td></tr> }
              </tbody></table></div>
              <div class="table-footer"><span>Mostrando <strong>{{ filteredOrders().length }}</strong> de <strong>{{ orders.length }}</strong> ordens</span><div class="pagination"><button type="button" aria-label="Página anterior" disabled>‹</button><button class="page-current" type="button" aria-current="page">1</button><button type="button" aria-label="Próxima página" disabled>›</button></div></div>
            </article>

            <aside class="right-column">
              <article class="panel schedule-panel" id="agenda"><div class="panel-heading compact-heading"><div><h2>Agenda de hoje</h2><p>Terça, 25 de novembro</p></div><button type="button" class="icon-button calendar-button" aria-label="Abrir calendário">▦</button></div>
                <div class="schedule-list"><div class="schedule-item"><time>09:00</time><span class="schedule-line schedule-green"></span><div><strong>Entrega de veículo</strong><span>Mariana Costa · Honda Civic</span></div></div><div class="schedule-item"><time>10:30</time><span class="schedule-line schedule-blue"></span><div><strong>Diagnóstico</strong><span>Rafael Mendes · VW T-Cross</span></div></div><div class="schedule-item"><time>14:00</time><span class="schedule-line schedule-amber"></span><div><strong>Retorno agendado</strong><span>Camila Rocha · Fiat Argo</span></div></div><div class="schedule-item"><time>16:30</time><span class="schedule-line schedule-purple"></span><div><strong>Retirada de veículo</strong><span>Pedro Almeida · Toyota Corolla</span></div></div></div><a href="#agenda" class="schedule-link">Ver agenda completa <span>→</span></a>
              </article>
              <article class="panel customers-panel" id="clientes"><div class="panel-heading compact-heading"><div><h2>Clientes recentes</h2><p>Últimos clientes cadastrados</p></div><a class="text-action" href="#clientes" aria-label="Ver todos os clientes">→</a></div>
                <div class="recent-customer"><span class="customer-avatar customer-one">MC</span><span><strong>Mariana Costa</strong><small>Honda Civic · ABC1D23</small></span><time>há 2h</time></div><div class="recent-customer"><span class="customer-avatar customer-two">RM</span><span><strong>Rafael Mendes</strong><small>VW T-Cross · EFG4H56</small></span><time>ontem</time></div><div class="recent-customer"><span class="customer-avatar customer-three">CR</span><span><strong>Camila Rocha</strong><small>Fiat Argo · IJK7L89</small></span><time>ontem</time></div>
              </article>
            </aside>
          </section>
          <footer class="dashboard-footer"><span>© 2026 AutoFlow</span><span><i class="footer-status-dot"></i> Todos os sistemas operacionais</span></footer>
        </div>
      </main>
    </div>
  `
})
export class DashboardPageComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly filters: StatusFilter[] = ['Todas', 'Em andamento', 'Aguardando peças', 'Pronta', 'Atrasada'];
  readonly orders: WorkOrder[] = [
    { id: 'OS-2026-084', customer: 'Mariana Costa', vehicle: 'Honda Civic · 2022', service: 'Revisão completa', status: 'Em andamento', dueDate: 'Hoje, 14:00', initials: 'MC', color: '#e7eefc' },
    { id: 'OS-2026-083', customer: 'Rafael Mendes', vehicle: 'VW T-Cross · 2023', service: 'Troca de pastilhas', status: 'Aguardando peças', dueDate: '27 nov, 10:00', initials: 'RM', color: '#f9e8df' },
    { id: 'OS-2026-082', customer: 'Camila Rocha', vehicle: 'Fiat Argo · 2021', service: 'Alinhamento e balanceamento', status: 'Pronta', dueDate: 'Hoje, 16:30', initials: 'CR', color: '#f4e9f7' },
    { id: 'OS-2026-081', customer: 'Pedro Almeida', vehicle: 'Toyota Corolla · 2020', service: 'Diagnóstico do motor', status: 'Atrasada', dueDate: '24 nov, 17:00', initials: 'PA', color: '#e6f1ea' },
    { id: 'OS-2026-080', customer: 'Juliana Ferreira', vehicle: 'Hyundai HB20 · 2024', service: 'Revisão de 10.000 km', status: 'Em andamento', dueDate: '26 nov, 12:00', initials: 'JF', color: '#f7eddb' }
  ];
  readonly activeFilter = signal<StatusFilter>('Todas');
  readonly query = signal('');
  readonly notice = signal('');
  readonly filteredOrders = computed(() => {
    const term = this.query().trim().toLocaleLowerCase('pt-BR');
    return this.orders.filter((order) => {
      const matchesStatus = this.activeFilter() === 'Todas' || order.status === this.activeFilter();
      const matchesQuery = !term || `${order.id} ${order.customer} ${order.vehicle} ${order.service}`.toLocaleLowerCase('pt-BR').includes(term);
      return matchesStatus && matchesQuery;
    });
  });
  readonly openOrders = this.orders.filter((order) => order.status !== 'Pronta').length;
  readonly inProgressOrders = this.orders.filter((order) => order.status === 'Em andamento').length;
  readonly waitingOrders = this.orders.filter((order) => order.status === 'Aguardando peças').length;

  updateQuery(event: Event): void { this.query.set((event.target as HTMLInputElement).value); }
  statusClass(status: WorkOrderStatus): string {
    return `status-badge ${status === 'Em andamento' ? 'status-progress' : status === 'Aguardando peças' ? 'status-waiting' : status === 'Pronta' ? 'status-ready' : 'status-late'}`;
  }
  showNewOrderMessage(): void { this.notice.set('O cadastro de ordens estará disponível quando a API oferecer esse fluxo.'); }
  logout(): void { this.auth.logout(); void this.router.navigate(['/login']); }
}
