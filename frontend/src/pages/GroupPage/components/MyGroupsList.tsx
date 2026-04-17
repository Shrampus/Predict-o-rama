   1 -import { LogOut } from 'lucide-react'                                                                                                                             
   1 +import { LogOut } from 'lucide-react';                                                                                                                            
   2  import { useState } from 'react';                                                                                                                                    3  import { useTranslation } from 'react-i18next';                                                                                                                   
   4 +import { useNavigate } from 'react-router-dom';                                                                                                                  
   5
   6 +import { ROUTE_PATHS } from '../../../app/routePaths';                                                                                                            
   7  import CopyInviteButton from '../../../components/ui/CopyInviteButton';
   6 -import { leaveGroup } from '../../../services/groupApi';                                                                                                          
   8  import type { MyGroupsResponse } from '../../../services/groupApi';
   9
  10  type MyGroupsListProps = {
  11    groups: MyGroupsResponse[];
  12    isLoading: boolean;
  13    errorMessage: string;
  13 -  onLeave: () => void;                                                                                                                                            
  14 +  onLeave: (groupId: string) => Promise<void>;                                                                                                                    
  15  };
  16
  16 -function LeaveGroupButton({ groupId, onLeave }: { groupId: string; onLeave: () => void }) {                                                                       
  17 +function LeaveGroupButton({                                                                                                                                       
  18 +  groupId,                                                                                                                                                        
  19 +  onLeave,                                                                                                                                                        
  20 +}: {                                                                                                                                                              
  21 +  groupId: string;                                                                                                                                                
  22 +  onLeave: (groupId: string) => Promise<void>;                                                                                                                    
  23 +}) {                                                                                                                                                              
  24    const [isLeaving, setIsLeaving] = useState(false);
  25    const [errorMessage, setErrorMessage] = useState('');
  26
 ...
  28      setIsLeaving(true);
  29      setErrorMessage('');
  30      try {
  24 -      await leaveGroup(groupId);                                                                                                                                  
  25 -      onLeave();                                                                                                                                                  
  31 +      await onLeave(groupId);                                                                                                                                     
  32      } catch (err) {
  33        setErrorMessage(err instanceof Error ? err.message : 'Failed to leave group');
  34      } finally {
 ...
  53
  54  function MyGroupsList({ groups, isLoading, errorMessage, onLeave }: MyGroupsListProps) {
  55    const { t } = useTranslation();
  56 +  const navigate = useNavigate();                                                                                                                                 
  57
  58 +  function getGroupDetailsPath(groupId: string) {                                                                                                                 
  59 +    return ROUTE_PATHS.groupDetails.replace(':groupId', encodeURIComponent(groupId));                                                                             
  60 +  }                                                                                                                                                               
  61 +                                                                                                                                                                  
  62 +  function openGroupDetails(group: MyGroupsResponse) {                                                                                                            
  63 +    navigate(getGroupDetailsPath(group.groupId), { state: { group } });                                                                                           
  64 +  }                                                                                                                                                               
  65 +                                                                                                                                                                  
  66 +  function shouldIgnoreRowNavigation(target: EventTarget | null): boolean {                                                                                       
  67 +    if (!(target instanceof HTMLElement)) {                                                                                                                       
  68 +      return false;                                                                                                                                               
  69 +    }                                                                                                                                                             
  70 +    return target.closest('button, a') !== null;                                                                                                                  
  71 +  }                                                                                                                                                               
  72 +                                                                                                                                                                  
  73    return (
  52 -    <div>                                                                                                                                                         
  53 -      <h2 className="text-lg font-semibold mb-3">{t('groups.myGroups')}</h2>                                                                                      
  74 +    <section className="bg-white border border-slate-200 rounded-2xl shadow-sm p-5 sm:p-6">                                                                       
  75 +      <div className="flex items-center justify-between mb-5">                                                                                                    
  76 +        <h2 className="text-lg sm:text-xl font-semibold text-slate-900">{t('groups.myGroups')}</h2>                                                               
  77 +        <span className="text-sm text-slate-500">                                                                                                                 
  78 +          {t('groups.groupCount', { count: groups.length })}                                                                                                      
  79 +        </span>                                                                                                                                                   
  80 +      </div>                                                                                                                                                      
  81
  82        {isLoading && <p className="text-sm text-slate-500">{t('groups.loading')}</p>}
  83
 ...
   87          <p className="text-sm text-slate-400">{t('groups.empty')}</p>
   88        )}
   89
   63 -      <ul className="space-y-2">                                                                                                                                 
   90 +      <ul className="space-y-3">                                                                                                                                 
   91          {groups.map((group) => (
   92            <li
   93              key={group.groupId}
   67 -            className="flex items-center justify-between bg-white border border-slate-200                                                                        
   68 -                       rounded-xl px-5 py-4 shadow-sm"                                                                                                           
   94 +            onClick={(event) => {                                                                                                                                
   95 +              if (shouldIgnoreRowNavigation(event.target)) {                                                                                                     
   96 +                return;                                                                                                                                          
   97 +              }                                                                                                                                                  
   98 +              openGroupDetails(group);                                                                                                                           
   99 +            }}                                                                                                                                                   
  100 +            onKeyDown={(event) => {                                                                                                                              
  101 +              if (shouldIgnoreRowNavigation(event.target)) {                                                                                                     
  102 +                return;                                                                                                                                          
  103 +              }                                                                                                                                                  
  104 +              if (event.key === 'Enter' || event.key === ' ') {                                                                                                  
  105 +                event.preventDefault();                                                                                                                          
  106 +                openGroupDetails(group);                                                                                                                         
  107 +              }                                                                                                                                                  
  108 +            }}                                                                                                                                                   
  109 +            role="button"                                                                                                                                        
  110 +            tabIndex={0}                                                                                                                                         
  111 +            className="flex items-center justify-between bg-slate-50 border border-slate-200                                                                     
  112 +                       rounded-xl px-5 py-4 cursor-pointer transition-colors hover:bg-slate-100                                                                  
  113 +                       focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500"                                                              
  114            >
  115              <div>
   71 -              <p className="font-semibold">{group.name}</p>                                                                                                      
  116 +              <p className="font-semibold text-slate-900">{group.name}</p>                                                                                       
  117                <p className="text-sm text-slate-500">{group.description}</p>
  118                {group.groupMemberRole === 'ADMIN' && (
  119                  <p className="text-xs text-slate-400 mt-1 flex items-center gap-1">
 ...
  123                )}
  124              </div>
  125              <div className="flex items-center gap-3">
  126 +              <button                                                                                                          
  127 +                type="button"                                                                                                                                    
  128 +                onClick={() => openGroupDetails(group)}                                                                                                          
  129 +                className="text-xs font-semibold text-blue-600 hover:text-blue-700"                                                                              
  130 +              >                                                                                                                                                  
  131 +                {t('groups.viewDetails')}                                                                                                                       
  132 +              </button>                                                                                                                                         
  133                <span                                
  134                  className={`text-xs font-bold uppercase tracking-wide px-3 py-1 rounded-full ${
  135                    group.groupMemberRole === 'ADMIN'
 ...              
  146            </li>
  147          ))}                                                                                                                                                      
  148        </ul>                                                                                                                                                      
   97 -    </div>                                                                                                                                                      
  149 +    </section>                                                                                                                                                  
  150    );
  151  }                                                                                                                                                                
  152                                      
